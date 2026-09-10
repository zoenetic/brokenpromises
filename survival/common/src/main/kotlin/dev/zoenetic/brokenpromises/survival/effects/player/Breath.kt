package dev.zoenetic.brokenpromises.survival.effects.player

import com.mojang.serialization.Codec
import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.units.Humidity
import io.netty.buffer.ByteBuf
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer

private const val RESTING_RATE: Double = 0.0 //TODO: Fix this

public val BREATH_VISIBLE_BELOW: Celsius = Celsius(8.0)
public val HUMIDITY_TO_BUMP_BREATH_VISIBLE_AT: Humidity = Humidity(0.5)

public data class Breath(
    public val rate: Double
) {
    public fun tick(player: ServerPlayer) {
        if (player.isUnderWater) return
        val conditions = Survival.platform.playerConditions.get(player) ?: return
        val humidity = conditions.humidity
        val temperature = if (humidity >= HUMIDITY_TO_BUMP_BREATH_VISIBLE_AT) {
            Celsius(conditions.temperature.value - 2.0)
        } else {
            conditions.temperature
        }
        // TODO: wire it to heart rate
        // TODO: make a larger and less opaque particle
        if (temperature < BREATH_VISIBLE_BELOW) {
            val eye = player.eyePosition
            val look = player.getViewVector(1.0f)
            val mouth = eye.add(look.scale(0.25)).subtract(0.0, 0.15, 0.0)
            player.level().sendParticles(
                ParticleTypes.WHITE_SMOKE,
                mouth.x, mouth.y, mouth.z,
                0,
                look.x + 0.3, look.y + 0.1, look.z + 0.3,
                0.005
            )
        }
    }

    public companion object {

        public val CODEC: Codec<Breath> =
            Codec.DOUBLE.xmap(
                ::Breath,
                Breath::rate,
            )

        public val STREAM_CODEC: StreamCodec<ByteBuf, Breath> =
            ByteBufCodecs.DOUBLE.map(
                ::Breath,
                Breath::rate
            )
    }
}