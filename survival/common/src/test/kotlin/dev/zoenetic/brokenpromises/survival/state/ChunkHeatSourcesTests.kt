package dev.zoenetic.brokenpromises.survival.state

import dev.zoenetic.brokenpromises.survival.CommonFixtures
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.chunk.LevelChunk
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChunkHeatSourcesTests {

    private val inChunk = BlockPos(260, 70, 520)
    private val alsoInChunk = BlockPos(265, 70, 525)
    private val topSection = BlockPos(260, 310, 520)
    private val belowZero = BlockPos(260, -60, 520)

    private val campfire = Blocks.CAMPFIRE.defaultBlockState()
    private val furnace = Blocks.FURNACE.defaultBlockState()
    private val stone = Blocks.STONE.defaultBlockState()
    private val air = Blocks.AIR.defaultBlockState()

    private fun LevelChunk.place(pos: BlockPos, state: BlockState) {
        getSection(getSectionIndex(pos.y))
            .setBlockState(pos.x and 15, pos.y and 15, pos.z and 15, state)
    }

    private fun chunk() = CommonFixtures.chunk(CommonFixtures.fakeLevel())

    @Test
    fun `lava counts as a lit heat source`() {
        assertTrue(Blocks.LAVA.defaultBlockState().isLit())
    }

    @Test
    fun `campfires can be unlit`() {
        assertTrue(campfire.isHeatSourceBlock())
        assertTrue(campfire.isLit())
        assertFalse(campfire.setValue(BlockStateProperties.LIT, false).isLit())
    }

    @Test
    fun `a non heat source block is not a heat source`() {
        assertFalse(stone.isHeatSourceBlock())
    }

    @Test
    fun `an empty chunk has no heat sources`() {
        val chunk = chunk()
        ChunkHeatSources.rebuild(chunk)
        assertTrue(ChunkHeatSources.of(chunk).isEmpty())
    }

    @Test
    fun `one heat source gets one entry in the index`() {
        val chunk = chunk()
        chunk.place(inChunk, campfire)
        ChunkHeatSources.rebuild(chunk)
        val index = ChunkHeatSources.of(chunk)
        assertEquals(1, index.size)
        assertEquals(HEAT_SOURCE_BLOCKS[Blocks.CAMPFIRE], index[inChunk.asLong()])
    }

    @Test
    fun `two heat sources in one section is two entries`() {
        val chunk = chunk()
        chunk.place(inChunk, campfire)
        chunk.place(alsoInChunk, campfire)
        ChunkHeatSources.rebuild(chunk)
        val index = ChunkHeatSources.of(chunk)
        assertEquals(2, index.size)
        assertTrue(index.containsKey(inChunk.asLong()))
        assertTrue(index.containsKey(alsoInChunk.asLong()))
    }

    @Test
    fun `a heat source in the top section is found`() {
        val chunk = chunk()
        chunk.place(topSection, campfire)
        ChunkHeatSources.rebuild(chunk)
        assertTrue(ChunkHeatSources.of(chunk).containsKey(topSection.asLong()))
    }

    @Test
    fun `a heat source at negative y is found`() {
        val chunk = chunk()
        chunk.place(belowZero, campfire)
        ChunkHeatSources.rebuild(chunk)
        val key = ChunkHeatSources.of(chunk).keys.single()
        assertEquals(belowZero, BlockPos.of(key), "packed key round-trips through BlockPos.of")
    }

    @Test
    fun `an unlit furnace is still in the index`() {
        val chunk = chunk()
        chunk.place(inChunk, furnace)
        ChunkHeatSources.rebuild(chunk)
        assertEquals(
            HEAT_SOURCE_BLOCKS[Blocks.FURNACE],
            ChunkHeatSources.of(chunk)[inChunk.asLong()]
        )
    }

    @Test
    fun `a heat source replaced by air is not in the index`() {
        val chunk = chunk()
        chunk.place(alsoInChunk, stone)
        chunk.place(inChunk, campfire)
        chunk.place(inChunk, air)
        ChunkHeatSources.rebuild(chunk)
        assertTrue(ChunkHeatSources.of(chunk).isEmpty())
    }

    @Test
    fun `putting then removing a single source clears it from the index`() {
        val chunk = chunk()
        ChunkHeatSources.onBlockChanged(chunk, inChunk, campfire)
        assertTrue(ChunkHeatSources.of(chunk).containsKey(inChunk.asLong()))
        ChunkHeatSources.removeHeatSource(chunk, inChunk)
        assertFalse(ChunkHeatSources.of(chunk).containsKey(inChunk.asLong()))
    }

    @Test
    fun `removing a non-existent source does not throw and leaves nothing behind`() {
        val chunk = chunk()
        ChunkHeatSources.removeHeatSource(chunk, inChunk)
        assertTrue(ChunkHeatSources.of(chunk).isEmpty())
    }

    @Test
    fun `changing a block to a non-source no-ops the index`() {
        val chunk = chunk()
        ChunkHeatSources.onBlockChanged(chunk, inChunk, stone)
        assertTrue(ChunkHeatSources.of(chunk).isEmpty())
    }

    @Test
    fun `updating one heat source to another leaves one entry with the second power`() {
        val chunk = chunk()
        ChunkHeatSources.onBlockChanged(chunk, inChunk, campfire)
        ChunkHeatSources.onBlockChanged(chunk, inChunk, furnace)
        val index = ChunkHeatSources.of(chunk)
        assertEquals(1, index.size)
        assertEquals(HEAT_SOURCE_BLOCKS[Blocks.FURNACE], index[inChunk.asLong()])
    }

    @Test
    fun `replacing a single source with air drops that blockpos from the index`() {
        val chunk = chunk()
        ChunkHeatSources.onBlockChanged(chunk, inChunk, campfire)
        ChunkHeatSources.onBlockChanged(chunk, inChunk, air)
        assertFalse(ChunkHeatSources.of(chunk).containsKey(inChunk.asLong()))
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures.bootstrap()
        }
    }
}
