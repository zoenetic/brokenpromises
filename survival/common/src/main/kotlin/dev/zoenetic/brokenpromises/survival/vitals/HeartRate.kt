package dev.zoenetic.brokenpromises.survival.vitals

import com.mojang.serialization.Codec
import dev.zoenetic.brokenpromises.survival.effects.player.SHIVER_CEASES
import dev.zoenetic.brokenpromises.survival.effects.player.shiverIntensity
import dev.zoenetic.brokenpromises.survival.units.BPM
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.units.Duration
import io.netty.buffer.ByteBuf
import net.minecraft.SharedConstants
import net.minecraft.network.codec.StreamCodec
import kotlin.math.pow

internal val RESTING_HEART_RATE = HeartRate(BPM(75.0))

internal const val ASYSTOLE_TEMPERATURE = 22.0
internal const val ARRHYTHMIA_TEMPERATURE = 44.0
internal const val PEAK_SHIVERING_AMPLITUDE = 35.0
internal const val MAX_HEART_RATE = 200.0

internal const val LOW_BPM_AUDIBLE_THRESHOLD = 60.0
internal const val LOW_BPM_FULL_VOLUME_THRESHOLD = 40.0

internal const val HIGH_BPM_AUDIBLE_THRESHOLD = 90.0
internal const val HIGH_BPM_FULL_VOLUME_THRESHOLD = 160.0

public data class Heartbeat(
    val interval: Duration,
    val pitch: Float,
    val volume: Float,
)

public data class HeartRate(
    val bpm: BPM = BPM(75.0)
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
                (chill * (RESTING_HEART_RATE.bpm.value + shiveringBump + heatDelta)).coerceAtMost(
                    MAX_HEART_RATE
                )
            )
        )
    }

    public fun toHeartbeat(): Heartbeat? {
        if (bpm.value <= 0.0) return null
        val interval = (SharedConstants.TICKS_PER_MINUTE / bpm.value).toLong()
        val pitch = 1F
        val volume = volume()
        return Heartbeat(
            Duration(interval), pitch, volume
        )
    }

    public fun volume(): Float {
        val actual = bpm.value
        val ramp = when {
            actual < LOW_BPM_AUDIBLE_THRESHOLD ->
                (LOW_BPM_AUDIBLE_THRESHOLD - actual) /
                        (LOW_BPM_AUDIBLE_THRESHOLD - LOW_BPM_FULL_VOLUME_THRESHOLD)

            actual > HIGH_BPM_AUDIBLE_THRESHOLD ->
                (actual - HIGH_BPM_AUDIBLE_THRESHOLD) /
                        (HIGH_BPM_FULL_VOLUME_THRESHOLD - HIGH_BPM_AUDIBLE_THRESHOLD)

            else -> 0.0
        }
        return ramp.coerceIn(0.0, 1.0).toFloat()
    }

    public companion object {
        public val CODEC: Codec<HeartRate> =
            BPM.CODEC.xmap(
                ::HeartRate,
                HeartRate::bpm
            )

        public val STREAM_CODEC: StreamCodec<ByteBuf, HeartRate> =
            BPM.STREAM_CODEC.map(
                ::HeartRate,
                HeartRate::bpm
            )

        public val DEFAULT: HeartRate = RESTING_HEART_RATE
    }
}