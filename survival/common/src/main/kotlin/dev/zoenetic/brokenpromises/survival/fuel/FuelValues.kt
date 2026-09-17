package dev.zoenetic.brokenpromises.survival.fuel

import dev.zoenetic.brokenpromises.survival.registry.BrokenPromisesItems
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

public object FuelValue {

    // TODO: look into vanilla fuel values and wire up if appropriate
    internal val map: Map<Item, Int> = mapOf(
        BrokenPromisesItems.FIREWOOD_ITEM to 4,
        Items.CHARCOAL to 8,
        Items.COAL to 8,
    )

    public fun of(item: Item): Int = map[item] ?: 0

}