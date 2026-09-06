package dev.zoenetic.brokenpromises.vitals

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.environment.Conditions
import dev.zoenetic.brokenpromises.heat.getInConductiveMedium
import dev.zoenetic.brokenpromises.heat.getOnConductiveSurface
import io.netty.buffer.ByteBuf
import net.minecraft.SharedConstants
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import kotlin.math.pow

internal const val NORMAL_BODY_TEMPERATURE = 37.0

public const val BODY_COOLS_AT: Double = 30.0 * 60.0
public const val BODY_WARMS_AT: Double = 15.0 * 60.0

public data class Vitals(val temperature: BodyTemperature) {
    public companion object {
        public val CODEC: Codec<Vitals> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    BodyTemperature.CODEC.fieldOf("temperature")
                        .forGetter(Vitals::temperature),
                ).apply(instance, ::Vitals)
            }
        public val STREAM_CODEC: StreamCodec<ByteBuf, Vitals> =
            StreamCodec.composite(
                BodyTemperature.STREAM_CODEC,
                Vitals::temperature,
                ::Vitals
            )
        public val DEFAULT: Vitals =
            Vitals(BodyTemperature(NORMAL_BODY_TEMPERATURE))
    }
}

public data class BodyTemperature(val value: Double) {
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

public fun ServerPlayer.setBodyTemperature(temperature: Double) {
    val vitals = BrokenPromises.platform.vitals(this)
    val newVitals = vitals.copy(
        temperature = BodyTemperature(temperature)
    )
    BrokenPromises.platform.setVitals(this, newVitals)
}

public fun ServerPlayer.tickVitals(
    conditions: Conditions,
    elapsed: Long
) {
    val target = targetTemperature(conditions.temperature.value)
    val vitals = vitals()
    val current = vitals.temperature.value
    if (current == target) return
    val isWarming = current < target
    val inMedium = getInConductiveMedium()?.conductance
    val onSurface = getOnConductiveSurface()?.conductance
    val fromWind = conditions.wind.conductance
    val halfLifeSeconds = effectiveHalfLife(isWarming, inMedium, onSurface, fromWind)
    val newBodyTemperature = BodyTemperature(
        approach(
            current,
            target,
            elapsed,
            halfLifeSeconds
        )
    )
    val newVitals =
        vitals.copy(temperature = newBodyTemperature)
    BrokenPromises.platform.setVitals(this, newVitals)
    return
}

public fun effectiveHalfLife(
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

public const val COMFORT_LOW: Double = 20.0
public const val COMFORT_HIGH: Double = 30.0

public const val COLD_LEAKAGE: Double = 0.5
public const val HEAT_LEAKAGE: Double = 0.2

internal fun targetTemperature(ambient: Double): Double = when {
    ambient < COMFORT_LOW -> NORMAL_BODY_TEMPERATURE - COLD_LEAKAGE * (COMFORT_LOW - ambient)
    ambient > COMFORT_HIGH -> NORMAL_BODY_TEMPERATURE + HEAT_LEAKAGE * (ambient - COMFORT_HIGH)
    else -> NORMAL_BODY_TEMPERATURE
}

public fun Player.vitals(): Vitals {
    return BrokenPromises.platform.vitals(this)
}

internal fun approach(
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
