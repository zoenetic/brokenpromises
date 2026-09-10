package dev.zoenetic.brokenpromises.survival.vitals

import com.mojang.serialization.Codec
import dev.zoenetic.brokenpromises.survival.probe.getConductanceOfMediumIn
import dev.zoenetic.brokenpromises.survival.probe.getConductanceOfSurfaceOn
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.units.Conductance
import dev.zoenetic.brokenpromises.survival.units.Duration
import io.netty.buffer.ByteBuf
import net.minecraft.SharedConstants
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player
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
    public fun getNew(
        player: Player,
        conditions: PlayerConditions,
        elapsed: Duration
    ): BodyTemperature {
        val target = target(conditions.temperature)
        if (value == target) return this
        val isWarming = value < target
        val medium = player.getConductanceOfMediumIn()
        val surface = player.getConductanceOfSurfaceOn()
        val wind = conditions.wind.conductance
        val halfLifeSeconds = halfLife(isWarming, medium, surface, wind)
        return BodyTemperature(
            approach(
                value,
                target,
                elapsed,
                halfLifeSeconds,
            )
        )
    }


    public companion object {
        internal fun target(ambient: Celsius): Celsius = when {
            ambient < COMFORT_LOW ->
                NORMAL_BODY_TEMPERATURE - COLD_LEAKAGE * (COMFORT_LOW - ambient).toDouble()

            ambient > COMFORT_HIGH ->
                NORMAL_BODY_TEMPERATURE + HEAT_LEAKAGE * (ambient - COMFORT_HIGH).toDouble()

            else -> NORMAL_BODY_TEMPERATURE
        }

        internal fun halfLife(
            isWarming: Boolean,
            medium: Conductance? = null,
            surface: Conductance? = null,
            wind: Conductance? = null,
        ): Conductance {
            val conductance =
                (medium ?: Conductance(1.0)) * (surface ?: Conductance(1.0)) * (wind
                    ?: Conductance(1.0))
            return if (isWarming) BODY_WARMS_AT / conductance else BODY_COOLS_AT / conductance
        }

        internal fun approach(
            current: Celsius,
            target: Celsius,
            elapsed: Duration,
            halfLife: Conductance,
        ): Celsius {
            val elapsedSeconds = elapsed.value / SharedConstants.TICKS_PER_SECOND.toDouble()
            val remainingFraction = 0.5.pow(elapsedSeconds / halfLife.value)
            return Celsius(target.value + (current.value - target.value) * remainingFraction)
        }
        
        public val CODEC: Codec<BodyTemperature> =
            Celsius.CODEC.xmap(
                ::BodyTemperature,
                BodyTemperature::value
            )

        public val STREAM_CODEC: StreamCodec<ByteBuf, BodyTemperature> =
            Celsius.STREAM_CODEC.map(
                ::BodyTemperature,
                BodyTemperature::value
            )

        public val DEFAULT: BodyTemperature = BodyTemperature(NORMAL_BODY_TEMPERATURE)
    }
}
