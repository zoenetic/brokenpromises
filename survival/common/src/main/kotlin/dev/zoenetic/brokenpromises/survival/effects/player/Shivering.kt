package dev.zoenetic.brokenpromises.survival.effects.player

import dev.zoenetic.brokenpromises.survival.units.Celsius
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

internal const val SHIVER_PEAK: Double = 34.0
internal const val SHIVER_ONSET: Double = 36.5
internal const val SHIVER_CEASES: Double = 32.0

public fun shiverIntensity(temperature: Celsius): Double {
    if (temperature.value >= SHIVER_ONSET || temperature.value <= SHIVER_CEASES) return 0.0
    val halfWidth =
        if (temperature.value > SHIVER_PEAK) SHIVER_ONSET - SHIVER_PEAK else SHIVER_PEAK - SHIVER_CEASES
    val distance = abs(temperature.value - SHIVER_PEAK) / halfWidth
    return 0.5 * (1.0 + cos(PI * distance))
}