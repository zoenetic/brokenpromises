package dev.zoenetic.brokenpromises.survival.vitals

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.units.Duration
import dev.zoenetic.brokenpromises.survival.units.Time
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level

private val INTERVAL: Duration = Duration(20L)

public data class Vitals(
    val bodyTemperature: BodyTemperature,
    val heartRate: HeartRate,
    val time: Time
) {
    public companion object {

        public fun get(player: Player): Vitals? = Survival.platform.vitals.get(player)

        public fun getFirst(time: Time): Vitals {
            return DEFAULT.copy(
                time = time,
            )
        }

        public fun getNew(
            previous: Vitals,
            player: ServerPlayer,
            time: Time,
            elapsed: Duration
        ): Vitals {
            val conditions =
                Survival.platform.playerConditions.get(player) ?: return previous
            val bodyTemperature = previous.bodyTemperature.getNew(
                player,
                conditions,
                elapsed
            )
            val heartRate = previous.heartRate.getNew(player, conditions, elapsed)
            val vitals = Vitals(
                bodyTemperature,
                heartRate,
                time
            )
            return vitals
        }

        public fun set(player: ServerPlayer, vitals: Vitals) {
            Survival.platform.vitals.set(player, vitals)
        }

        public fun tick(level: Level) {
            if (level !is ServerLevel) return
            val time = Time(level.gameTime)
            level.players().forEach { player -> tick(player, time) }
        }

        public fun tick(player: ServerPlayer, time: Time) {
            val previous = get(player)
            val vitals = if (previous != null) {
                val elapsed = time - previous.time
                if (elapsed < INTERVAL) return
                getNew(previous, player, time, elapsed)
            } else {
                getFirst(time)
            }
            set(player, vitals)
        }

        public val CODEC: Codec<Vitals> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    BodyTemperature.CODEC.fieldOf("body_temperature")
                        .forGetter(Vitals::bodyTemperature),
                    HeartRate.CODEC.fieldOf("heart_rate")
                        .forGetter(Vitals::heartRate),
                    Time.CODEC.fieldOf("time")
                        .forGetter(Vitals::time),
                ).apply(instance, ::Vitals)
            }

        public val STREAM_CODEC: StreamCodec<ByteBuf, Vitals> =
            StreamCodec.composite(
                BodyTemperature.STREAM_CODEC,
                Vitals::bodyTemperature,
                HeartRate.STREAM_CODEC,
                Vitals::heartRate,
                Time.STREAM_CODEC,
                Vitals::time,
                ::Vitals
            )

        public val DEFAULT: Vitals = Vitals(
            bodyTemperature = BodyTemperature.DEFAULT,
            heartRate = HeartRate.DEFAULT,
            time = Time(0L)
        )
    }
}