package dev.zoenetic.brokenpromises.environment

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

@JvmInline
public value class Temperature(public val value: Double) {
    public companion object {
        private const val EQUATOR_C = 30.0
        private const val POLE_C = -20.0
        private const val LATITUDE_FALLOFF = 1.3
        private const val LAPSE_RATE_PER_BLOCK = 0.07

        public fun fromNoise(n: Double): Temperature {
            val latitude = (PI / 4.0) * (1.0 - n.coerceIn(-1.0, 1.0))
            return Temperature(POLE_C + (EQUATOR_C - POLE_C) * cos(latitude).pow(LATITUDE_FALLOFF))
        }
    }

    internal fun adjustedForAltitude(a: Int): Temperature {
        val t = if (a > 0) {
            this.value - (a * LAPSE_RATE_PER_BLOCK)
        } else {
            this.value
        }
        return Temperature(t)
    }
}