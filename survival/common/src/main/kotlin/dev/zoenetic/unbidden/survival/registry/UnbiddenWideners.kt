package dev.zoenetic.unbidden.survival.registry

import dev.zoenetic.unbidden.survival.platform.addValidBlocks
import net.minecraft.world.level.block.entity.BlockEntityTypes

public object UnbiddenWideners {
    public fun init() {
        BlockEntityTypes.CAMPFIRE.addValidBlocks { listOf(UnbiddenBlocks.CAMPFIRE) }
    }
}