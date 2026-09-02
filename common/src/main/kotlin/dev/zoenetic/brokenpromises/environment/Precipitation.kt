package dev.zoenetic.brokenpromises.environment

import kotlin.math.pow

@JvmInline
public value class Precipitation(public val value: Double) {
    public companion object {
        private const val REFERENCE_C = 30.0
        private const val REFERENCE_MM = 4000.0
        private const val CLAUSIUS_CLAPEYRON = 1.07
        private const val ARIDITY_FLOOR = 0.05

        public fun fromHumidityAndTemperature(h: Double, t: Temperature): Precipitation {
            val ceiling = REFERENCE_MM * CLAUSIUS_CLAPEYRON.pow(t.value - REFERENCE_C)
            val wetness = ARIDITY_FLOOR + (1.0 - ARIDITY_FLOOR) * (h.coerceIn(-1.0, 1.0) + 1.0) / 2.0
            return Precipitation(ceiling * wetness)
        }
    }
}

