package dev.zoenetic.unbidden.survival.platform

import dev.zoenetic.unbidden.survival.Survival
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType

public fun BlockEntityType<*>.addValidBlocks(blocks: () -> List<Block>): Unit
        = Survival.platform.wideners.addValidBlocks(this, blocks)