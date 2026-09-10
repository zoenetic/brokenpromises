package dev.zoenetic.brokenpromises.survival.state

import dev.zoenetic.brokenpromises.survival.CommonFixtures
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HeatSourceBlocksTests {

    private val campfire = Blocks.CAMPFIRE.defaultBlockState()
    private val stone = Blocks.STONE.defaultBlockState()

    @Test
    fun `lava counts as a lit heat source`() {
        assertTrue(Blocks.LAVA.defaultBlockState().isHeatSourceBlock())
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
    fun `every registered heat source block is recognised as one`() {
        for (block in HEAT_SOURCE_BLOCKS.keys) {
            assertTrue(
                block.defaultBlockState().isHeatSourceBlock(),
                "$block has a power but isHeatSourceBlock says otherwise"
            )
        }
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures.bootstrap()
        }
    }
}
