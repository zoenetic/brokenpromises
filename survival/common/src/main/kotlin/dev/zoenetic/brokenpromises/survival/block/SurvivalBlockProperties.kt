package dev.zoenetic.brokenpromises.survival.block

import net.minecraft.world.level.block.state.properties.IntegerProperty

public object SurvivalBlockProperties {

    @JvmField
    public val FUEL_LEVEL: IntegerProperty = IntegerProperty.create("fuel_level", 0, 15)

    public const val MAX_FUEL: Int = 15
}