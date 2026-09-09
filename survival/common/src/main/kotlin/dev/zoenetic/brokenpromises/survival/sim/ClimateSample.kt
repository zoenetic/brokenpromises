package dev.zoenetic.brokenpromises.survival.sim

import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.units.Humidity
import dev.zoenetic.brokenpromises.survival.units.Wind

public data class ClimateSample(
    val humidity: Humidity,
    val temperature: Celsius,
    val wind: Wind,
)