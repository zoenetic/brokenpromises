package dev.zoenetic.unbidden.survival.climate

import dev.zoenetic.unbidden.survival.units.Heat
import dev.zoenetic.unbidden.survival.units.Humidity
import dev.zoenetic.unbidden.survival.units.Wind

public data class ClimateSample(
    val humidity: Humidity,
    val temperature: Heat,
    val wind: Wind,
)