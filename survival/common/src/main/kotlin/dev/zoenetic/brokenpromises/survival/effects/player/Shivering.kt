package dev.zoenetic.brokenpromises.survival.effects.player

import dev.zoenetic.brokenpromises.survival.units.Celsius
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

internal val SHIVER_PEAK: Celsius = Celsius(34.0)
internal val SHIVER_ONSET: Celsius = Celsius(36.5)
internal val SHIVER_CEASES: Celsius = Celsius(32.0)

/**
 * How hard a body at [temperature] is shivering, from 0.0 (not at all) to 1.0.
 *
 * Shivering starts at [SHIVER_ONSET], peaks at [SHIVER_PEAK] and stops again at
 * [SHIVER_CEASES], where the body has given up on it. The two sides of the peak
 * are scaled independently so both reach 1.0 at the peak itself.
 */
public fun shiverIntensity(temperature: Celsius): Double {
    if (temperature >= SHIVER_ONSET || temperature <= SHIVER_CEASES) return 0.0
    val halfWidth =
        if (temperature > SHIVER_PEAK) SHIVER_ONSET - SHIVER_PEAK else SHIVER_PEAK - SHIVER_CEASES
    val distance = abs(temperature.value - SHIVER_PEAK.value) / halfWidth.value
    return 0.5 * (1.0 + cos(PI * distance))
}
