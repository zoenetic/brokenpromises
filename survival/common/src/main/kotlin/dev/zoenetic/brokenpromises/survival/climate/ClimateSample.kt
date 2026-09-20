package dev.zoenetic.brokenpromises.survival.climate

import dev.zoenetic.brokenpromises.survival.units.Heat
import dev.zoenetic.brokenpromises.survival.units.Humidity
import dev.zoenetic.brokenpromises.survival.units.Wind

public data class ClimateSample(
    val humidity: Humidity,
    val temperature: Heat,
    val wind: Wind,
)