package dev.zoenetic.brokenpromises.effects.player

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

public const val SHIVER_AMPLITUDE_DEGREES: Double = 1.5
public const val SHIVER_FREQUENCY: Double = 2.0

internal const val SHIVER_PEAK = 34.0
internal const val SHIVER_ONSET = 36.5
internal const val SHIVER_CEASES = 32.0

public interface ShiverState {
    public fun `brokenpromises$getShiver`(): Double
    public fun `brokenpromises$setShiver`(shiver: Double)
}

internal fun shiverIntensity(bodyTemperature: Double): Double {
    if (bodyTemperature >= SHIVER_ONSET || bodyTemperature <= SHIVER_CEASES) return 0.0
    val halfWidth =
        if (bodyTemperature > SHIVER_PEAK) SHIVER_ONSET - SHIVER_PEAK else SHIVER_PEAK - SHIVER_CEASES
    val distance = abs(bodyTemperature - SHIVER_PEAK) / halfWidth
    return 0.5 * (1.0 + cos(PI * distance))
}