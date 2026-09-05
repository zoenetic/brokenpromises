package dev.zoenetic.brokenpromises.heat

import dev.zoenetic.brokenpromises.CommonFixtures
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.chunk.LevelChunk
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SourcesTests {

    private val inChunk = BlockPos(260, 70, 520)
    private val alsoInChunk = BlockPos(265, 70, 525)
    private val topSection = BlockPos(260, 310, 520)
    private val belowZero = BlockPos(260, -60, 520)

    private val campfire = Blocks.CAMPFIRE.defaultBlockState()
    private val furnace = Blocks.FURNACE.defaultBlockState()
    private val stone = Blocks.STONE.defaultBlockState()
    private val air = Blocks.AIR.defaultBlockState()

    private fun LevelChunk.place(pos: BlockPos, state: BlockState) {
        getSection(getSectionIndex(pos.y)).setBlockState(pos.x and 15, pos.y and 15, pos.z and 15, state)
    }

    private fun LevelChunk.indexed() = globalHeatSourceState[level]?.get(pos.pack())

    @Test
    fun `lava counts as a lit heat source`() {
        val lava = Blocks.LAVA.defaultBlockState()
        assertTrue(lava.isLit())
    }

    @Test
    fun `campfires can be unlit`() {
        assertTrue(campfire.isHeatSourceBlock())
        assertTrue(campfire.isLit())
        val unlit = campfire.setValue(BlockStateProperties.LIT, false)
        assertFalse(unlit.isLit())
    }

    @Test
    fun `a non heat source block is not a heat source`() {
        assertFalse(stone.isHeatSourceBlock())
    }

    @Test
    fun `an empty chunk has no heat sources`() {
        val chunk = CommonFixtures.chunk(CommonFixtures.fakeLevel())
        chunk.rebuildHeatSourceState()
        val entry = chunk.indexed()
        assertNotNull(entry, "rebuild registers the chunk even when it finds nothing")
        assertTrue(entry.isEmpty())
    }

    @Test
    fun `one heat source gets one entry in the global state`() {
        val chunk = CommonFixtures.chunk(CommonFixtures.fakeLevel())
        chunk.place(inChunk, campfire)
        chunk.rebuildHeatSourceState()
        val entry = chunk.indexed()!!
        assertEquals(1, entry.size)
        assertEquals(HEAT_SOURCE_BLOCKS[Blocks.CAMPFIRE], entry[inChunk.asLong()])
    }

    @Test
    fun `two heat sources in one section is two entries`() {
        val chunk = CommonFixtures.chunk(CommonFixtures.fakeLevel())
        chunk.place(inChunk, campfire)
        chunk.place(alsoInChunk, campfire)
        chunk.rebuildHeatSourceState()
        val entry = chunk.indexed()!!
        assertEquals(2, entry.size)
        assertTrue(entry.containsKey(inChunk.asLong()))
        assertTrue(entry.containsKey(alsoInChunk.asLong()))
    }

    @Test
    fun `a heat source in the top section is found`() {
        val chunk = CommonFixtures.chunk(CommonFixtures.fakeLevel())
        chunk.place(topSection, campfire)
        chunk.rebuildHeatSourceState()
        assertTrue(chunk.indexed()!!.containsKey(topSection.asLong()))
    }

    @Test
    fun `a heat source at negative y is found`() {
        val chunk = CommonFixtures.chunk(CommonFixtures.fakeLevel())
        chunk.place(belowZero, campfire)
        chunk.rebuildHeatSourceState()
        val entry = chunk.indexed()!!
        val key = entry.keys.single()
        assertEquals(belowZero, BlockPos.of(key), "packed key round-trips through BlockPos.of")
    }

    @Test
    fun `an unlit furnace is in the global state`() {
        val chunk = CommonFixtures.chunk(CommonFixtures.fakeLevel())
        chunk.place(inChunk, furnace)
        chunk.rebuildHeatSourceState()
        assertEquals(HEAT_SOURCE_BLOCKS[Blocks.FURNACE], chunk.indexed()!![inChunk.asLong()])
    }

    @Test
    fun `a heat source replaced by air is not in the global state`() {
        val chunk = CommonFixtures.chunk(CommonFixtures.fakeLevel())
        chunk.place(alsoInChunk, stone)
        chunk.place(inChunk, campfire)
        chunk.place(inChunk, air)
        chunk.rebuildHeatSourceState()
        assertTrue(chunk.indexed()!!.isEmpty())
    }

    @Test
    fun `putting then dropping a single source removes it from the global state`() {
        val chunk = CommonFixtures.chunk(CommonFixtures.fakeLevel())
        chunk.getOrPutStateForSingleHeatSource(HeatSource(inChunk, HEAT_SOURCE_BLOCKS[Blocks.CAMPFIRE]!!))
        assertTrue(chunk.indexed()!!.containsKey(inChunk.asLong()))
        chunk.dropStateForSingleHeatSource(inChunk)
        assertFalse(chunk.indexed()!!.containsKey(inChunk.asLong()))
    }

    @Test
    fun `attempting to drop a non-existent source does not throw and does not leave stuff behind`() {
        val level = CommonFixtures.fakeLevel()
        val chunk = CommonFixtures.chunk(level)
        chunk.dropStateForSingleHeatSource(inChunk)
        assertNull(globalHeatSourceState[level], "a drop must not create a level entry")
    }

    @Test
    fun `updating a non-source no-ops the global state`() {
        val level = CommonFixtures.fakeLevel()
        val chunk = CommonFixtures.chunk(level)
        chunk.updateStateForSingleHeatSource(inChunk, stone)
        assertNull(globalHeatSourceState[level])
    }

    @Test
    fun `updating one heat source to another leaves a single source with the 2nd sources power`() {
        val chunk = CommonFixtures.chunk(CommonFixtures.fakeLevel())
        chunk.updateStateForSingleHeatSource(inChunk, campfire)
        chunk.updateStateForSingleHeatSource(inChunk, furnace)
        val entry = chunk.indexed()!!
        assertEquals(1, entry.size)
        assertEquals(HEAT_SOURCE_BLOCKS[Blocks.FURNACE], entry[inChunk.asLong()])
    }

    @Test
    fun `replacing a single source with air drops that blockpos from the global state`() {
        val chunk = CommonFixtures.chunk(CommonFixtures.fakeLevel())
        chunk.updateStateForSingleHeatSource(inChunk, campfire)
        chunk.updateStateForSingleHeatSource(inChunk, air)
        assertFalse(chunk.indexed()!!.containsKey(inChunk.asLong()))
    }

    @Test
    fun `rebuilding state for a chunk leaves no stale entries`() {
        val chunk = CommonFixtures.chunk(CommonFixtures.fakeLevel())
        chunk.updateStateForSingleHeatSource(inChunk, campfire)
        chunk.rebuildHeatSourceState()
        assertTrue(chunk.indexed()!!.isEmpty(), "rebuild replaces; it does not merge")
    }

    @Test
    fun `rebuilding and then dropping state for a chunk leaves nothing in the per-level map for that chunk`() {
        val level = CommonFixtures.fakeLevel()
        val chunk = CommonFixtures.chunk(level)
        chunk.place(inChunk, campfire)
        chunk.rebuildHeatSourceState()
        chunk.dropHeatSourceState()
        assertFalse(globalHeatSourceState[level]!!.containsKey(chunk.pos.pack()))
    }

    @Test
    fun `dropping state for a chunk on a level that was never in global state no-ops`() {
        val level = CommonFixtures.fakeLevel()
        CommonFixtures.chunk(level).dropHeatSourceState()
        assertNull(globalHeatSourceState[level])
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures
        }
    }
}
