package dev.zoenetic.brokenpromises.environment

import dev.zoenetic.brokenpromises.CommonFixtures
import dev.zoenetic.brokenpromises.heat.HEAT_SOURCE_BLOCKS
import dev.zoenetic.brokenpromises.heat.HEAT_SOURCE_SOFTENING
import dev.zoenetic.brokenpromises.heat.Temperature
import dev.zoenetic.brokenpromises.heat.rebuildHeatSourceState
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerChunkCache
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.junit.jupiter.api.BeforeAll
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class ConditionsQueryTests {

    private val campfireAt = BlockPos(260, 70, 520)
    private val playerAt = BlockPos(260, 70, 521)
    private val climate = ClimateSample(Humidity(0.5), Temperature(10.0), Wind.CALM)
    private val body = AABB(260.2, 70.0, 521.2, 260.8, 71.8, 521.8)

    private fun expectedHeat(at: BlockPos, power: Double): Double {
        val d2 = body.center.distanceToSqr(Vec3.atCenterOf(at)) + HEAT_SOURCE_SOFTENING
        return power / d2
    }

    private fun baseline(level: net.minecraft.server.level.ServerLevel): Double {
        val empty = CommonFixtures.chunk(level)
        empty.rebuildHeatSourceState()
        return playerNextTo(empty, level).getConditions(climate).temperature.value
    }

    private fun playerNextTo(
        chunk: LevelChunk,
        level: net.minecraft.server.level.ServerLevel
    ): ServerPlayer {
        val chunkCache = mock(ServerChunkCache::class.java)
        doReturn(chunk).`when`(chunkCache).getChunkNow(anyInt(), anyInt())
        doReturn(chunkCache).`when`(level).chunkSource
        doReturn(0L).`when`(level).overworldClockTime
        doReturn(63).`when`(level).seaLevel
        doReturn(15).`when`(level)
            .getBrightness(any(LightLayer::class.java), any(BlockPos::class.java))
        doReturn(true).`when`(level).canSeeSky(any(BlockPos::class.java))
        val player = mock(ServerPlayer::class.java)
        doReturn(level).`when`(player).level()
        doReturn(playerAt).`when`(player).blockPosition()
        doReturn(body).`when`(player).boundingBox
        return player
    }

    private fun LevelChunk.place(
        pos: BlockPos,
        state: net.minecraft.world.level.block.state.BlockState
    ) {
        getSection(getSectionIndex(pos.y)).setBlockState(
            pos.x and 15,
            pos.y and 15,
            pos.z and 15,
            state
        )
    }

    @Test
    fun `a lit campfire next to the player raises ambient by a lot`() {
        val level = CommonFixtures.fakeServerLevel()
        val chunk = CommonFixtures.chunk(level)
        chunk.place(campfireAt, Blocks.CAMPFIRE.defaultBlockState())
        chunk.rebuildHeatSourceState()
        val player = playerNextTo(chunk, level)

        val with = player.getConditions(climate).temperature.value
        val expected =
            baseline(level) + expectedHeat(campfireAt, HEAT_SOURCE_BLOCKS[Blocks.CAMPFIRE]!!.value)
        assertEquals(expected, with, 1e-6, "campfire heat should be exactly power / distance²")
    }

    @Test
    fun `an unlit campfire is indexed but contributes nothing`() {
        val level = CommonFixtures.fakeServerLevel()
        val chunk = CommonFixtures.chunk(level)
        chunk.place(
            campfireAt,
            Blocks.CAMPFIRE.defaultBlockState().setValue(
                net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT,
                false
            )
        )
        chunk.rebuildHeatSourceState()
        val player = playerNextTo(chunk, level)
        assertEquals(baseline(level), player.getConditions(climate).temperature.value, 1e-6)
    }

    @Test
    fun `a torch next to the player raises ambient by a little`() {
        val level = CommonFixtures.fakeServerLevel()
        val chunk = CommonFixtures.chunk(level)
        chunk.place(campfireAt, Blocks.TORCH.defaultBlockState())
        chunk.rebuildHeatSourceState()
        val player = playerNextTo(chunk, level)

        val with = player.getConditions(climate).temperature.value
        val expected =
            baseline(level) + expectedHeat(campfireAt, HEAT_SOURCE_BLOCKS[Blocks.TORCH]!!.value)
        assertEquals(expected, with, 1e-6, "torch heat should be exactly power / distance²")
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures
        }
    }
}
