package dev.zoenetic.brokenpromises.vitals

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

internal const val RESTING_HEART_RATE: Double = 75.0

@JvmInline
public value class HeartRate(public val value: Double) {
    public companion object {
        public val CODEC: Codec<HeartRate> =
            Codec.DOUBLE.xmap(
                ::HeartRate,
                HeartRate::value
            )
        public val STREAM_CODEC: StreamCodec<ByteBuf, HeartRate> =
            ByteBufCodecs.DOUBLE.map(
                ::HeartRate,
                HeartRate::value
            )
        public val DEFAULT: HeartRate =
            HeartRate(RESTING_HEART_RATE)
    }
}

internal fun effectiveHalfLifeForHeartRate(): Double {
    TODO()
}

internal fun targetHeartRate(): Double = TODO()

internal fun approachHeartRate(halfLifeSeconds: Double): Double {
    TODO()
}