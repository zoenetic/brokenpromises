package dev.zoenetic.unbidden.survival.fuel

import net.minecraft.world.level.block.state.properties.IntegerProperty

public object FuelProperties {
    @JvmField
    public val FUEL_LEVEL: IntegerProperty = IntegerProperty.create("fuel_level", 0, 15)
}
