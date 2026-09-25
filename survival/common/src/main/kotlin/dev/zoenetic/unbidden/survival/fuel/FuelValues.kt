package dev.zoenetic.unbidden.survival.fuel

import dev.zoenetic.unbidden.survival.registry.UnbiddenItems
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

public object FuelValues {

    // TODO: look into vanilla fuel values and wire up if appropriate
    internal val map: Map<Item, Fuel> = mapOf(
        UnbiddenItems.FIREWOOD to Fuel(4),
        Items.CHARCOAL to Fuel(8),
        Items.COAL to Fuel(8),
    )

    public fun get(item: Item): Fuel = map[item] ?: Fuel.EMPTY

}