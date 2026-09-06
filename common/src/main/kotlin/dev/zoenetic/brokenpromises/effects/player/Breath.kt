package dev.zoenetic.brokenpromises.effects.player

import dev.zoenetic.brokenpromises.environment.Conditions
import dev.zoenetic.brokenpromises.heat.Temperature
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer

public const val BREATH_VISIBLE_BELOW: Double = 8.0
public const val HUMIDITY_TO_BUMP_BREATH_VISIBLE_AT: Double = 0.5

public fun ServerPlayer.tickBreath(conditions: Conditions) {
    if (isUnderWater) return
    val humidity = conditions.humidity
    val temperature = if (humidity.value >= HUMIDITY_TO_BUMP_BREATH_VISIBLE_AT) {
        Temperature(conditions.temperature.value - 2.0)
    } else {
        conditions.temperature
    }
    // TODO: tie to actual breathing rate (once we generate that)
    // TODO: make less opaque and bigger and fade out?
    if (temperature.value < BREATH_VISIBLE_BELOW) {
        val eye = eyePosition
        val look = getViewVector(1.0f)
        val mouth = eye.add(look.scale(0.25)).subtract(0.0, 0.15, 0.0)
        level().sendParticles(
            ParticleTypes.WHITE_SMOKE,
            mouth.x, mouth.y, mouth.z,
            0,
            look.x + 0.3, look.y + 0.1, look.z + 0.3,
            0.005,
        )
    }
}