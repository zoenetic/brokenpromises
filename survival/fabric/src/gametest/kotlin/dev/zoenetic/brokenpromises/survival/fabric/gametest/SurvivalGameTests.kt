package dev.zoenetic.brokenpromises.survival.fabric.gametest

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.probe.getHumidity
import dev.zoenetic.brokenpromises.survival.state.ChunkHeatSources
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.COMFORT_HIGH
import dev.zoenetic.brokenpromises.survival.vitals.COMFORT_LOW
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks

class SurvivalGameTests {

    // no replacement yet, still needed
    @Suppress("DEPRECATION", "removal")
    private fun GameTestHelper.playerAt(relative: BlockPos): ServerPlayer {
        val player = makeMockServerPlayerInLevel()
        val absolute = absolutePos(relative)
        player.snapTo(absolute.x + 0.5, absolute.y.toDouble(), absolute.z + 0.5)
        return player
    }

    @GameTest
    fun aRealServerLevelIsAvailable(helper: GameTestHelper) {
        check(!helper.level.isClientSide) { "expected a server level" }
        check(Survival.platform.name == "Fabric") {
            "the mod should be initialised on Fabric, got ${Survival.platform.name}"
        }
        helper.succeed()
    }

    @GameTest
    fun conditionsComeFromThePlayersOwnChunk(helper: GameTestHelper) {
        val player = helper.playerAt(BlockPos(1, 2, 1))

        val conditions = PlayerConditions.get(player)
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

    @GameTest
    fun conditionsRecordTheCurrentGameTime(helper: GameTestHelper) {
        val player = helper.playerAt(BlockPos(1, 2, 1))
        val conditions = PlayerConditions.get(player)
        if (conditions.time.value != helper.level.gameTime) {
            throw helper.assertionException(
                "conditions recorded tick ${conditions.time.value}, level is at ${helper.level.gameTime}"
            )
        }
        helper.succeed()
    }

    @GameTest
    fun theLevelTickStoresConditionsForPlayersInTheWorld(helper: GameTestHelper) {
        val player = helper.playerAt(BlockPos(1, 2, 1))

        PlayerConditions.tick(helper.level)

        val stored = Survival.platform.playerConditions.get(player)
        if (stored.time.value != helper.level.gameTime) {
            throw helper.assertionException(
                "after a level tick the player's stored conditions are at tick " +
                        "${stored.time.value}, level is at ${helper.level.gameTime}"
            )
        }
        helper.succeed()
    }

    @GameTest(maxTicks = 200)
    fun theProductionLoopDrivesBodyTemperature(helper: GameTestHelper) {
        val player = helper.playerAt(BlockPos(1, 2, 1))
        val ambient = PlayerConditions.get(player).temperature
        val start = Vitals.get(player).bodyTemperature.value.value

        helper.startSequence()
            .thenExecuteFor(100) { /* mod's own tick handlers do the work */ }
            .thenExecute {
                val now = Vitals.get(player).bodyTemperature.value.value
                val drift = now - start
                val where = "ambient ${ambient.value}C, body $start -> $now (drift $drift)"
                when {
                    ambient < COMFORT_LOW -> if (drift >= 0.0) throw helper.assertionException(
                        "below the comfort band the body should cool: $where"
                    )

                    ambient > COMFORT_HIGH -> if (drift <= 0.0) throw helper.assertionException(
                        "above the comfort band the body should warm: $where"
                    )

                    else -> if (kotlin.math.abs(drift) > 1e-6) throw helper.assertionException(
                        "inside the comfort band the body should hold steady: $where"
                    )
                }
            }
            .thenSucceed()
    }

    // proves the mixin is installed.
    @GameTest
    fun placingACampfireRegistersAHeatSource(helper: GameTestHelper) {
        val relative = BlockPos(1, 1, 1)
        helper.setBlock(relative, Blocks.CAMPFIRE)

        val absolute = helper.absolutePos(relative)
        val index = ChunkHeatSources.of(helper.level.getChunkAt(absolute))
        if (!index.containsKey(absolute.asLong())) {
            throw helper.assertionException(
                "campfire at $absolute never reached the chunk's heat index (LevelChunkMixin?)"
            )
        }
        helper.succeed()
    }

    @GameTest
    fun breakingACampfireDeregistersTheHeatSource(helper: GameTestHelper) {
        val relative = BlockPos(1, 1, 1)
        helper.setBlock(relative, Blocks.CAMPFIRE)
        helper.setBlock(relative, Blocks.AIR)

        val absolute = helper.absolutePos(relative)
        val index = ChunkHeatSources.of(helper.level.getChunkAt(absolute))
        if (index.containsKey(absolute.asLong())) {
            throw helper.assertionException("campfire at $absolute is still in the heat index")
        }
        helper.succeed()
    }

    @GameTest
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
}
