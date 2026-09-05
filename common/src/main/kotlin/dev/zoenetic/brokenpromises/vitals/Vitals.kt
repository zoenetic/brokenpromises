package dev.zoenetic.brokenpromises.vitals

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.environment.Conditions
import dev.zoenetic.brokenpromises.heat.getInMedium
import dev.zoenetic.brokenpromises.heat.getOnSurface
import io.netty.buffer.ByteBuf
import net.minecraft.SharedConstants
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import kotlin.math.pow

internal const val NORMAL_BODY_TEMPERATURE = 37.0

public const val BODY_COOLS_AT: Double = 20.0 * 60.0
public const val BODY_WARMS_AT: Double = 10.0 * 60.0

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
    val currentVitals = getVitals()
    if (currentVitals.temperature.value == conditions.temperature.value) return
    val isWarming =
        currentVitals.temperature.value < conditions.temperature.value
    val inMedium = getInMedium()?.conductance ?: 1.0
    val onSurface = getOnSurface()?.conductance ?: 1.0
    val conductance = inMedium * onSurface
    val halfLifeSeconds =
        if (isWarming) BODY_WARMS_AT / conductance else BODY_COOLS_AT / conductance
    val ambient = conditions.temperature.value
    val target =
        if (ambient !in 20.0..30.0) ambient else NORMAL_BODY_TEMPERATURE
    val newBodyTemperature = BodyTemperature(
        approach(
            currentVitals.temperature.value,
            target,
            elapsed,
            halfLifeSeconds
        )
    )
    val newVitals =
        currentVitals.copy(temperature = newBodyTemperature)
    BrokenPromises.platform.setVitals(this, newVitals)
    return
}

public fun ServerPlayer.getVitals(): Vitals {
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