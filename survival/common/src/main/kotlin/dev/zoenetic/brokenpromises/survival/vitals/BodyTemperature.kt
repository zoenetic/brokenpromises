package dev.zoenetic.brokenpromises.survival.vitals

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.probe.getConductanceOfMediumIn
import dev.zoenetic.brokenpromises.survival.probe.getConductanceOfSurfaceOn
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.units.Conductance
import dev.zoenetic.brokenpromises.survival.units.Ticks
import io.netty.buffer.ByteBuf
import net.minecraft.SharedConstants
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import kotlin.math.pow

internal val NORMAL_BODY_TEMPERATURE = Celsius(37.0)

public val BODY_COOLS_AT: Conductance = Conductance(30.0 * 60.0)
public val BODY_WARMS_AT: Conductance = Conductance(15.0 * 60.0)

public val COMFORT_LOW: Celsius = Celsius(20.0)
public val COMFORT_HIGH: Celsius = Celsius(30.0)
public val COLD_LEAKAGE: Celsius = Celsius(0.5)
public val HEAT_LEAKAGE: Celsius = Celsius(0.2)


public data class BodyTemperature(
    val value: Celsius = Celsius(37.0)
) {
    public fun getNew(player: ServerPlayer, elapsed: Ticks): BodyTemperature {
        val current = this.value
        val conditions = Survival.platform.playerConditions.get(player)
        val target = targetTemperature(conditions.temperature)
        if (current == target) return this
        val isWarming = current < target
        val medium = player.getConductanceOfMediumIn()
        val surface = player.getConductanceOfSurfaceOn()
        val wind = conditions.wind.conductance
        val halfLifeSeconds = effectiveHalfLife(isWarming, medium, surface, wind)
        return BodyTemperature(
            approach(
                current,
                target,
                elapsed,
                halfLifeSeconds,
            )
        )
    }

    public companion object {
        public val CODEC: Codec<BodyTemperature> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    Celsius.CODEC.fieldOf("celsius")
                        .forGetter(BodyTemperature::value)
                ).apply(instance, ::BodyTemperature)
            }
        public val STREAM_CODEC: StreamCodec<ByteBuf, BodyTemperature> =
            StreamCodec.composite(
                Celsius.STREAM_CODEC,
                BodyTemperature::value,
                ::BodyTemperature,
            )
        public val DEFAULT: BodyTemperature =
            BodyTemperature(NORMAL_BODY_TEMPERATURE)
    }
}

/** The core temperature a body settles at for a given [ambient], in Celsius. */
internal fun targetTemperature(ambient: Celsius): Celsius = when {
    ambient < COMFORT_LOW ->
        NORMAL_BODY_TEMPERATURE - COLD_LEAKAGE * (COMFORT_LOW - ambient).toDouble()

    ambient > COMFORT_HIGH ->
        NORMAL_BODY_TEMPERATURE + HEAT_LEAKAGE * (ambient - COMFORT_HIGH).toDouble()

    else -> NORMAL_BODY_TEMPERATURE
}

/** Seconds for the body to close half the gap to its target, in these conditions. */
internal fun effectiveHalfLife(
    isWarming: Boolean,
    medium: Conductance? = null,
    surface: Conductance? = null,
    wind: Conductance? = null,
): Conductance {
    val conductance =
        (medium ?: Conductance(1.0)) * (surface ?: Conductance(1.0)) * (wind ?: Conductance(1.0))
    return if (isWarming) BODY_WARMS_AT / conductance else BODY_COOLS_AT / conductance
}

/** Exponential approach: composes, so N steps of one tick equal one step of N ticks. */
internal fun approach(
    current: Celsius,
    target: Celsius,
    elapsed: Ticks,
    halfLife: Conductance,
): Celsius {
    val elapsedSeconds = elapsed.value / SharedConstants.TICKS_PER_SECOND.toDouble()
    val remainingFraction = 0.5.pow(elapsedSeconds / halfLife.value)
    return Celsius(target.value + (current.value - target.value) * remainingFraction)
}
