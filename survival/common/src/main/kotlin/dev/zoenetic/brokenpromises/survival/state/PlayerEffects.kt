package dev.zoenetic.brokenpromises.survival.state

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.brokenpromises.survival.effects.player.Breath
import dev.zoenetic.brokenpromises.survival.effects.player.Heartbeat
import dev.zoenetic.brokenpromises.survival.effects.player.SpeedPenalty
import dev.zoenetic.brokenpromises.survival.units.Time
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer

public data class PlayerEffects(
    val breath: Breath,
    val heartbeat: Heartbeat,
    val speedPenalty: SpeedPenalty,
    val time: Time,
) {
    public fun tick(player: ServerPlayer) {
        breath.tick(player)
        heartbeat.tick(player)
        speedPenalty.tick(player)
    }

    public companion object {

        public val CODEC: Codec<PlayerEffects> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    Breath.CODEC.fieldOf("breath")
                        .forGetter(PlayerEffects::breath),
                    Heartbeat.CODEC.fieldOf("heartbeat")
                        .forGetter(PlayerEffects::heartbeat),
                    SpeedPenalty.CODEC.fieldOf("speed_penalty")
                        .forGetter(PlayerEffects::speedPenalty),
                    Time.CODEC.fieldOf("time")
                        .forGetter(PlayerEffects::time)
                ).apply(instance, ::PlayerEffects)
            }

        public val STREAM_CODEC: StreamCodec<ByteBuf, PlayerEffects> =
            StreamCodec.composite(
                Breath.STREAM_CODEC,
                PlayerEffects::breath,
                Heartbeat.STREAM_CODEC,
                PlayerEffects::heartbeat,
                SpeedPenalty.STREAM_CODEC,
                PlayerEffects::speedPenalty,
                Time.STREAM_CODEC,
                PlayerEffects::time,
                ::PlayerEffects
            )
    }
}
