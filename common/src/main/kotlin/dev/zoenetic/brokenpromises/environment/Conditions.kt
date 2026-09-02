package dev.zoenetic.brokenpromises.environment

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel

public data class Conditions(
    val temperature: Temperature,
    val precipitation: Precipitation,
)

// TODO: make this do more than just apply altitude
public fun ServerLevel.getConditions(pos: BlockPos): Conditions {
    val c = getClimate(pos)
    val a = pos.y - seaLevel
    val p = c.precipitation
    val t = c.temperature.adjustedForAltitude(a)
    return Conditions(t, p)
}