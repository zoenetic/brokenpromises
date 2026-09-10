package dev.zoenetic.brokenpromises.survival.effects.player

import com.mojang.serialization.Codec
import dev.zoenetic.brokenpromises.survival.units.BPM
import dev.zoenetic.brokenpromises.survival.vitals.HeartRate
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer

private const val RESTING_HEARTBEAT: Double = 0.0

public val HEARTBEAT_AUDIBLE_OVER: BPM = BPM(0.0) //TODO: Fix this

public data class Heartbeat(
    public val intensity: Double,
) {
    public fun from(heartRate: HeartRate): Heartbeat {
        TODO()
    }

    public fun tick(player: ServerPlayer) {
        TODO()
    }

    public companion object {
        public val CODEC: Codec<Heartbeat> =
            Codec.DOUBLE.xmap(
                ::Heartbeat,
                Heartbeat::intensity
            )

        public val STREAM_CODEC: StreamCodec<ByteBuf, Heartbeat> =
            ByteBufCodecs.DOUBLE.map(
                ::Heartbeat,
                Heartbeat::intensity
            )
    }
}