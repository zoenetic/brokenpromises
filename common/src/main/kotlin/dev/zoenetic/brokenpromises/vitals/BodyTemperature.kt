package dev.zoenetic.brokenpromises.vitals

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.SharedConstants
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import kotlin.math.pow

internal const val NORMAL_BODY_TEMPERATURE = 37.0
internal const val BODY_COOLS_AT: Double = 30.0 * 60.0
internal const val BODY_WARMS_AT: Double = 15.0 * 60.0
internal const val COMFORT_LOW: Double = 20.0
internal const val COMFORT_HIGH: Double = 30.0
internal const val COLD_LEAKAGE: Double = 0.5
internal const val HEAT_LEAKAGE: Double = 0.2

public class BodyTemperature(public val value: Double) {

    public companion object {
        public val CODEC: Codec<BodyTemperature> =
            Codec.DOUBLE.xmap(
                ::BodyTemperature,
                BodyTemperature::value
            )
        public val STREAM_CODEC: StreamCodec<ByteBuf, BodyTemperature> =
            ByteBufCodecs.DOUBLE.map(
                ::BodyTemperature,
                BodyTemperature::value
            )
        public val DEFAULT: BodyTemperature =
            BodyTemperature(NORMAL_BODY_TEMPERATURE)
    }
}

internal fun effectiveHalfLifeForBodyTemperature(
    isWarming: Boolean,
    medium: Double? = null,
    surface: Double? = null,
    wind: Double? = null,
): Double {
    val medium = medium ?: 1.0
    val surface = surface ?: 1.0
    val wind = wind ?: 1.0
    val conductance = medium * surface * wind
    return if (isWarming) {
        BODY_WARMS_AT / conductance
    } else {
        BODY_COOLS_AT / conductance
    }
}

internal fun targetTemperature(ambient: Double): Double = when {
    ambient < COMFORT_LOW -> NORMAL_BODY_TEMPERATURE - COLD_LEAKAGE * (COMFORT_LOW - ambient)
    ambient > COMFORT_HIGH -> NORMAL_BODY_TEMPERATURE + HEAT_LEAKAGE * (ambient - COMFORT_HIGH)
    else -> NORMAL_BODY_TEMPERATURE
}

internal fun approachBodyTemperature(
    current: Double,
    target: Double,
    elapsedTicks: Long,
    halfLifeSeconds: Double
): Double {
    val elapsedSeconds =
        elapsedTicks / SharedConstants.TICKS_PER_SECOND.toDouble()
    val remainingFraction =
        0.5.pow(elapsedSeconds / halfLifeSeconds)
    return target + (current - target) * remainingFraction
}