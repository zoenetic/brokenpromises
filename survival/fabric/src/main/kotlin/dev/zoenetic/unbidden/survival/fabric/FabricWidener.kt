package dev.zoenetic.unbidden.survival.fabric

import dev.zoenetic.unbidden.survival.platform.Widener
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType

public object FabricWidener : Widener {
    override fun addValidBlocks(
        type: BlockEntityType<*>,
        blocks: () -> List<Block>
    ) {
        blocks().forEach { block ->
            type.addValidBlock(block)
        }
    }
}