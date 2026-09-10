package dev.zoenetic.brokenpromises.survival.vitals

import com.mojang.serialization.Codec
import dev.zoenetic.brokenpromises.survival.effects.player.SHIVER_CEASES
import dev.zoenetic.brokenpromises.survival.effects.player.shiverIntensity
import dev.zoenetic.brokenpromises.survival.units.BPM
import dev.zoenetic.brokenpromises.survival.units.Celsius
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import kotlin.math.pow

internal const val RESTING_HEART_RATE = 75.0
internal const val ASYSTOLE_TEMPERATURE = 22.0
internal const val ARRHYTHMIA_TEMPERATURE = 44.0
internal const val PEAK_SHIVERING_AMPLITUDE = 35.0
internal const val MAX_HEART_RATE = 200.0

public data class HeartRate(
    val value: BPM = BPM(75.0)
) {
    public fun getNew(
        bodyTemperature: Celsius
    ): HeartRate {
        val core = bodyTemperature.value
        val chill =
            ((core - ASYSTOLE_TEMPERATURE) / (SHIVER_CEASES - ASYSTOLE_TEMPERATURE)).coerceIn(
                0.0,
                1.0
            )
        val shiveringBump = shiverIntensity(bodyTemperature) * PEAK_SHIVERING_AMPLITUDE
        val heatDelta =
            10 * (core - NORMAL_BODY_TEMPERATURE).coerceIn(
                0.0,
                ARRHYTHMIA_TEMPERATURE - NORMAL_BODY_TEMPERATURE
            ).pow(1.3)
        return HeartRate(
            BPM(
                (chill * (RESTING_HEART_RATE + shiveringBump + heatDelta)).coerceAtMost(
                    MAX_HEART_RATE
                )
            )
        )
    }

    public companion object {
        public val CODEC: Codec<HeartRate> =
            BPM.CODEC.xmap(
                ::HeartRate,
                HeartRate::value
            )

        public val STREAM_CODEC: StreamCodec<ByteBuf, HeartRate> =
            BPM.STREAM_CODEC.map(
                ::HeartRate,
                HeartRate::value
            )

        public val DEFAULT: HeartRate = HeartRate(BPM(RESTING_HEART_RATE))
    }
}