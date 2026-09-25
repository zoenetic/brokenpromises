package dev.zoenetic.unbidden.survival.registry

import net.minecraft.world.level.block.state.properties.IntegerProperty

public object UnbiddenBlockStateProperties {
    @JvmField
    public val FUEL_LEVEL: IntegerProperty = IntegerProperty.create("fuel_level", 0, 15)
}
