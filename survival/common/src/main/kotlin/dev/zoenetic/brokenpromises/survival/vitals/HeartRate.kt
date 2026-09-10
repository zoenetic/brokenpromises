package dev.zoenetic.brokenpromises.survival.vitals

import com.mojang.serialization.Codec
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.units.BPM
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.units.Duration
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player

internal val RESTING_HEART_RATE = BPM(75.0)

public data class HeartRate(
    val value: BPM = BPM(75.0)
) {
    public fun getNew(
        player: Player,
        conditions: PlayerConditions,
        elapsed: Duration
    ): HeartRate {
        val target = target(conditions.temperature)
        if (value == target) return this
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
            BPM.CODEC.xmap(
                ::HeartRate,
                HeartRate::value
            )

        public val STREAM_CODEC: StreamCodec<ByteBuf, HeartRate> =
            BPM.STREAM_CODEC.map(
                ::HeartRate,
                HeartRate::value
            )

        public val DEFAULT: HeartRate = HeartRate(RESTING_HEART_RATE)
    }
}