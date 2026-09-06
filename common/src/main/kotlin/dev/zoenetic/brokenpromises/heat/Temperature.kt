package dev.zoenetic.brokenpromises.heat

import dev.zoenetic.brokenpromises.environment.Humidity
import dev.zoenetic.brokenpromises.environment.Sky
import net.minecraft.SharedConstants
import net.minecraft.util.Mth
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

internal const val DIURNAL_SWING_DRY = 11.0
internal const val DIURNAL_SWING_HUMID = 3.5
internal const val WARMEST_TICK = 9000L // 15:00
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

public fun Temperature.adjust(
    altitude: Int,
    time: Long,
    sky: Sky,
    humidity: Humidity
): Temperature {
    return adjustForAltitude(altitude)
        .adjustForTimeOfDay(time, sky, humidity)
}

public fun Temperature.adjustForAltitude(
    altitude: Int
): Temperature {
    val t = if (altitude > 0) {
        value - (altitude * LAPSE_RATE_PER_BLOCK)
    } else {
        value
    }
    return Temperature(t)
}

public fun Temperature.adjustForTimeOfDay(
    time: Long,
    sky: Sky,
    humidity: Humidity
): Temperature {
    val dayFraction = (time - WARMEST_TICK).toDouble() / SharedConstants.TICKS_PER_GAME_DAY
    val swing = Mth.lerp(humidity.value, DIURNAL_SWING_DRY, DIURNAL_SWING_HUMID) * sky.openness
    return Temperature(value + swing * cos(2.0 * PI * dayFraction))
}