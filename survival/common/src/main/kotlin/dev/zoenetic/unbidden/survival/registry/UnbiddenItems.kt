package dev.zoenetic.unbidden.survival.registry

import dev.zoenetic.unbidden.survival.Survival
import dev.zoenetic.unbidden.survival.platform.getValue
import net.minecraft.core.Direction
import net.minecraft.world.item.Item

public object UnbiddenItems {

    public val FIREWOOD_ITEM: Item by Survival.platform.register.blockItem(
        "firewood",
        UnbiddenBlocks::FIREWOOD,
        Item.Properties()
    )

    public val FUELLED_TORCH_ITEM: Item by Survival.platform.register.standingAndWallBlockItem(
        "fuelled_torch",
        UnbiddenBlocks::FUELLED_TORCH_BLOCK,
        UnbiddenBlocks::FUELLED_WALL_TORCH_BLOCK,
        Direction.DOWN,
        Item.Properties()
    )

    public fun init() {}
}