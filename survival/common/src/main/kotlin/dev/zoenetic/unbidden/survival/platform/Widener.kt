package dev.zoenetic.unbidden.survival.platform

import dev.zoenetic.unbidden.survival.Survival
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType

public interface Widener {
    public fun addValidBlocks(type: BlockEntityType<*>, blocks: () -> List<Block> )
}