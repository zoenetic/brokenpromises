package dev.zoenetic.brokenpromises.survival.vitals

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.units.BPM
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.units.Ticks
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer

internal val RESTING_HEART_RATE = BPM(75.0)

public data class HeartRate(
    val value: BPM = BPM(75.0)
) {
    public fun getNew(player: ServerPlayer, elapsed: Ticks): HeartRate {
        val current = this.value
        val conditions = Survival.platform.playerConditions.get(player)
        val target = target(conditions.temperature)
        if (current == target) return this
        val halfLifeSeconds = halfLife()
        return HeartRate(
            approach()
        )
    }

    private fun target(temperature: Celsius): BPM {
        TODO()
    }

    private fun halfLife(): Double {
        TODO()
    }

    private fun approach(): BPM {
        TODO()
    }

    public companion object {
        public val CODEC: Codec<HeartRate> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    BPM.CODEC.fieldOf("bpm")
                        .forGetter(HeartRate::value)
                ).apply(instance, ::HeartRate)
            }
        public val STREAM_CODEC: StreamCodec<ByteBuf, HeartRate> =
            StreamCodec.composite(
                BPM.STREAM_CODEC,
                HeartRate::value,
                ::HeartRate,
            )
        public val DEFAULT: HeartRate =
            HeartRate(RESTING_HEART_RATE)
    }
}