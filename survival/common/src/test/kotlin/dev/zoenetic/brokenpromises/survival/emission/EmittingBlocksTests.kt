package dev.zoenetic.brokenpromises.survival.emission

import dev.zoenetic.brokenpromises.survival.CommonFixtures
import dev.zoenetic.brokenpromises.survival.emission.Emitters.isEmittingBlock
import dev.zoenetic.brokenpromises.survival.emission.Emitters.isLit
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmittingBlocksTests {

    private val campfire = Blocks.CAMPFIRE.defaultBlockState()
    private val stone = Blocks.STONE.defaultBlockState()

    @Test
    fun `lava counts as a lit heat source`() {
        assertTrue(Blocks.LAVA.defaultBlockState().isEmittingBlock())
        assertTrue(Blocks.LAVA.defaultBlockState().isLit())
    }

    @Test
    fun `campfires can be unlit`() {
        assertTrue(campfire.isEmittingBlock())
        assertTrue(campfire.getValue(BlockStateProperties.LIT))
    }

    @Test
    fun `a non heat source block is not a heat source`() {
        assertFalse(stone.isEmittingBlock())
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures.bootstrap()
        }
    }
}
