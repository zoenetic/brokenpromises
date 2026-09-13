package dev.zoenetic.brokenpromises.survival.registry

import dev.zoenetic.brokenpromises.survival.Survival
import net.minecraft.core.Holder
import net.minecraft.world.item.Item

public object Items {
    public val FIREWOOD_ITEM: Holder<Item> = Survival.platform.registrar.blockItem(
        "firewood",
        Blocks.FIREWOOD,
        Item.Properties()
    )

    public fun init() {}
}