package dev.zoenetic.brokenpromises.survival.gametest

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.probe.getHumidity
import dev.zoenetic.brokenpromises.survival.state.ChunkHeatSources
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.units.Time
import dev.zoenetic.brokenpromises.survival.vitals.COMFORT_HIGH
import dev.zoenetic.brokenpromises.survival.vitals.COMFORT_LOW
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks
import kotlin.math.abs

data class SurvivalTest(
    val name: String,
    val maxTicks: Int,
    val run: (GameTestHelper) -> Unit,
)

object SurvivalTests {

    private fun <T : Any> GameTestHelper.require(value: T?, what: String): T =
        value ?: throw assertionException("expected $what to be in the store, found nothing")

    private fun GameTestHelper.sample(player: ServerPlayer): PlayerConditions =
        PlayerConditions.getNew(player, Time(level.gameTime))

    @Suppress("DEPRECATION", "removal")
    private fun GameTestHelper.playerAt(relative: BlockPos): ServerPlayer {
        val player = makeMockServerPlayerInLevel()
        val absolute = absolutePos(relative)
        player.snapTo(absolute.x + 0.5, absolute.y.toDouble(), absolute.z + 0.5)
        return player
    }

    fun aRealServerLevelIsAvailable(helper: GameTestHelper) {
        check(!helper.level.isClientSide) { "expected a server level" }
        check(Survival.platform.name in setOf("fabric", "neoforge")) {
            "expected a real loader platform, got ${Survival.platform.name}"
        }
        helper.succeed()
    }

    fun conditionsComeFromThePlayersOwnChunk(helper: GameTestHelper) {
        val player = helper.playerAt(BlockPos(1, 2, 1))

        val conditions = helper.sample(player)
        val ownChunk = helper.level.getChunkAt(player.blockPosition())
        val expected = ownChunk.getHumidity()

        if (conditions.humidity != expected) {
            throw helper.assertionException(
                "conditions read humidity ${conditions.humidity.value}, but the player's own " +
                        "chunk ${ownChunk.pos} reads ${expected.value}"
            )
        }
        helper.succeed()
    }

    fun conditionsRecordTheCurrentGameTime(helper: GameTestHelper) {
        val player = helper.playerAt(BlockPos(1, 2, 1))
        val conditions = helper.sample(player)
        if (conditions.time.value != helper.level.gameTime) {
            throw helper.assertionException(
                "conditions recorded tick ${conditions.time.value}, level is at ${helper.level.gameTime}"
            )
        }
        helper.succeed()
    }

    fun theLevelTickStoresConditionsForPlayersInTheWorld(helper: GameTestHelper) {
        val player = helper.playerAt(BlockPos(1, 2, 1))

        PlayerConditions.tick(helper.level)

        val stored = helper.require(
            Survival.platform.playerConditions.get(player),
            "conditions for a player the level tick has just visited",
        )
        if (stored.time.value != helper.level.gameTime) {
            throw helper.assertionException(
                "after a level tick the player's stored conditions are at tick " +
                        "${stored.time.value}, level is at ${helper.level.gameTime}"
            )
        }
        helper.succeed()
    }

    fun theProductionLoopDrivesBodyTemperature(helper: GameTestHelper) {
        val player = helper.playerAt(BlockPos(1, 2, 1))
        var ambient: Celsius? = null
        var start: Double? = null

        helper.startSequence()
            .thenIdle(2)
            .thenExecute {
                ambient = helper.sample(player).temperature
                start = helper.require(Vitals.get(player), "seeded vitals")
                    .bodyTemperature.value.value
            }
            .thenExecuteFor(100) { /* the mod's own tick handlers do the work */ }
            .thenExecute {
                val began = helper.require(start, "a captured starting temperature")
                val outside = helper.require(ambient, "a captured ambient temperature")
                val now = helper.require(Vitals.get(player), "vitals")
                    .bodyTemperature.value.value
                val drift = now - began
                val where = "ambient ${outside.value}C, body $began -> $now (drift $drift)"
                when {
                    outside < COMFORT_LOW -> if (drift >= 0.0) throw helper.assertionException(
                        "below the comfort band the body should cool: $where"
                    )

                    outside > COMFORT_HIGH -> if (drift <= 0.0) throw helper.assertionException(
                        "above the comfort band the body should warm: $where"
                    )

                    else -> if (abs(drift) > 1e-6) throw helper.assertionException(
                        "inside the comfort band the body should hold steady: $where"
                    )
                }
            }
            .thenSucceed()
    }

    fun placingACampfireRegistersAHeatSource(helper: GameTestHelper) {
        val relative = BlockPos(1, 1, 1)
        helper.setBlock(relative, Blocks.CAMPFIRE)

        val absolute = helper.absolutePos(relative)
        val index = helper.require(
            ChunkHeatSources.of(helper.level.getChunkAt(absolute)),
            "a heat index for the chunk holding $absolute",
        )
        if (!index.containsKey(absolute.asLong())) {
            throw helper.assertionException(
                "campfire at $absolute never reached the chunk's heat index (LevelChunkMixin?)"
            )
        }
        helper.succeed()
    }

    fun breakingACampfireDeregistersTheHeatSource(helper: GameTestHelper) {
        val relative = BlockPos(1, 1, 1)
        helper.setBlock(relative, Blocks.CAMPFIRE)
        helper.setBlock(relative, Blocks.AIR)

        val absolute = helper.absolutePos(relative)
        val index = helper.require(
            ChunkHeatSources.of(helper.level.getChunkAt(absolute)),
            "a heat index for the chunk holding $absolute",
        )
        if (index.containsKey(absolute.asLong())) {
            throw helper.assertionException("campfire at $absolute is still in the heat index")
        }
        helper.succeed()
    }

    fun aNearbyCampfireIsFoundAsAHeatSource(helper: GameTestHelper) {
        val campfire = BlockPos(1, 1, 1)
        helper.setBlock(campfire, Blocks.CAMPFIRE)
        val player = helper.playerAt(BlockPos(2, 2, 1))

        val absolute = helper.absolutePos(campfire)
        val sources = ChunkHeatSources.around(player)
        if (sources.none { it.position == absolute }) {
            throw helper.assertionException(
                "campfire at $absolute was not among the ${sources.size} sources found " +
                        "around the player at ${player.blockPosition()}"
            )
        }
        helper.succeed()
    }

    val ALL: List<SurvivalTest> = listOf(
        SurvivalTest("a_real_server_level_is_available", 20, ::aRealServerLevelIsAvailable),
        SurvivalTest(
            "conditions_come_from_the_players_own_chunk",
            20,
            ::conditionsComeFromThePlayersOwnChunk
        ),
        SurvivalTest(
            "conditions_record_the_current_game_time",
            20,
            ::conditionsRecordTheCurrentGameTime
        ),
        SurvivalTest(
            "the_level_tick_stores_conditions",
            20,
            ::theLevelTickStoresConditionsForPlayersInTheWorld
        ),
        SurvivalTest(
            "the_production_loop_drives_body_temperature",
            200,
            ::theProductionLoopDrivesBodyTemperature
        ),
        SurvivalTest(
            "placing_a_campfire_registers_a_heat_source",
            20,
            ::placingACampfireRegistersAHeatSource
        ),
        SurvivalTest(
            "breaking_a_campfire_deregisters_it",
            20,
            ::breakingACampfireDeregistersTheHeatSource
        ),
        SurvivalTest("a_nearby_campfire_is_found", 20, ::aNearbyCampfireIsFoundAsAHeatSource),
    )
}
