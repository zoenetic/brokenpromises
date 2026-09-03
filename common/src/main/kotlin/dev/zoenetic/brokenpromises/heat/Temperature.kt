package dev.zoenetic.brokenpromises.heat

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

internal const val LAPSE_RATE_PER_BLOCK = 0.07

@JvmInline
public value class Temperature(public val value: Double) {
    public companion object {
        private const val EQUATOR_C: Double = 30.0
        private const val POLE_C: Double = -20.0
        private const val LATITUDE_FALLOFF = 1.3

        public fun fromNoise(n: Double): Temperature {
            val latitude = (PI / 4.0) * (1.0 - n.coerceIn(-1.0, 1.0))
            return Temperature(POLE_C + (EQUATOR_C - POLE_C) * cos(latitude).pow(LATITUDE_FALLOFF))
        }
    }
}

internal fun adjustTemperatureForAltitude(temperature: Temperature, altitude: Int): Temperature {
    val t = if (altitude > 0) {
        temperature.value - (altitude * LAPSE_RATE_PER_BLOCK)
    } else {
        temperature.value
    }
    return Temperature(t)
}