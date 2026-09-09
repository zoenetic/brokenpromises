package dev.zoenetic.brokenpromises.survival.effects.player

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.units.Humidity
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer

public val BREATH_VISIBLE_BELOW: Celsius = Celsius(8.0)
public val HUMIDITY_TO_BUMP_BREATH_VISIBLE_AT: Humidity = Humidity(0.5)

public object Breath {
    public fun tick(player: ServerPlayer) {
        if (player.isUnderWater) return
        val conditions = Survival.platform.playerConditions.get(player)
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
}