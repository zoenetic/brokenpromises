package dev.zoenetic.unbidden.survival.fuel.firewood

import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.state.BlockState

public fun BlockState.canBeSplit(): Boolean {
    return this.`is`(BlockTags.LOGS)
}