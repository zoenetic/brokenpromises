package dev.zoenetic.brokenpromises.survival.vitals

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.units.Ticks
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

public data class Vitals(
    val bodyTemperature: BodyTemperature = BodyTemperature.DEFAULT,
    val heartRate: HeartRate = HeartRate.DEFAULT,
) {
    public companion object {

        public fun get(player: Player): Vitals = Survival.platform.vitals.get(player)

        public fun set(player: ServerPlayer, vitals: Vitals) {
            Survival.platform.vitals.set(player, vitals)
        }

        /** One tick of drift towards the target implied by the player's conditions. */
        public fun tick(level: ServerLevel) {
            level.players().forEach { player -> tick(player, Ticks(1L)) }
        }

        public fun tick(player: ServerPlayer, elapsed: Ticks) {
            val previous = get(player)
            set(
                player,
                previous.copy(
                    // HeartRate.getNew is still TODO(); carried through untouched.
                    bodyTemperature = previous.bodyTemperature.getNew(player, elapsed),
                )
            )
        }

        public val CODEC: Codec<Vitals> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    BodyTemperature.CODEC.fieldOf("body_temperature")
                        .forGetter(Vitals::bodyTemperature),
                    HeartRate.CODEC.fieldOf("heart_rate")
                        .forGetter(Vitals::heartRate)
                ).apply(instance, ::Vitals)
            }
        public val STREAM_CODEC: StreamCodec<ByteBuf, Vitals> =
            StreamCodec.composite(
                BodyTemperature.STREAM_CODEC,
                Vitals::bodyTemperature,
                HeartRate.STREAM_CODEC,
                Vitals::heartRate,
                ::Vitals
            )
        public val DEFAULT: Vitals =
            Vitals(
                BodyTemperature.DEFAULT,
                HeartRate.DEFAULT
            )
    }
}