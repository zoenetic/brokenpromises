package dev.zoenetic.brokenpromises.survival.registry

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.platform.getValue
import net.minecraft.core.Direction
import net.minecraft.world.item.Item

public object BrokenPromisesItems {

    public val FIREWOOD_ITEM: Item by Survival.platform.register.blockItem(
        "firewood",
        BrokenPromisesBlocks::FIREWOOD,
        Item.Properties()
    )

    public val FUELLED_TORCH_ITEM: Item by Survival.platform.register.standingAndWallBlockItem(
        "fuelled_torch",
        BrokenPromisesBlocks::FUELLED_TORCH_BLOCK,
        BrokenPromisesBlocks::FUELLED_WALL_TORCH_BLOCK,
        Direction.DOWN,
        Item.Properties()
    )

    public fun init() {}
}