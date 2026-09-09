package dev.zoenetic.brokenpromises.survival.effects.player

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.Survival.MOD_ID
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.vitals.NORMAL_BODY_TEMPERATURE
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import kotlin.math.abs

internal val SPEED_PENALTY_MAX = SpeedPenalty(0.6)

internal const val SPEED_PENALTY_DEAD_ZONE = 1.0
internal const val COLD_FULL_PENALTY_AT = 28.0
internal const val HEAT_FULL_PENALTY_AT = 41.0

private val BODY_TEMPERATURE_SPEED_REDUCTION =
    Identifier.fromNamespaceAndPath(
        MOD_ID,
        "body_temperature_speed_reduction"
    )

@JvmInline
public value class SpeedPenalty(public val value: Double) {
    
    public fun tick(player: ServerPlayer) {
        val vitals = Survival.platform.vitals.get(player)
        val penalty = forTemperature(vitals.bodyTemperature.value)
        val attribute = player.getAttribute(MOVEMENT_SPEED) ?: return
        if (penalty.value == 0.0) {
            attribute.removeModifier(BODY_TEMPERATURE_SPEED_REDUCTION)
        } else {
            attribute.addOrUpdateTransientModifier(
                AttributeModifier(
                    BODY_TEMPERATURE_SPEED_REDUCTION,
                    -penalty.value,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                )
            )
        }
    }

    public companion object {
        public fun forTemperature(temperature: Celsius): SpeedPenalty {
            val deviation =
                abs(temperature.value - NORMAL_BODY_TEMPERATURE.value)
            if (deviation <= SPEED_PENALTY_DEAD_ZONE) return SpeedPenalty(0.0)
            val fullAt =
                if (temperature < NORMAL_BODY_TEMPERATURE) COLD_FULL_PENALTY_AT else HEAT_FULL_PENALTY_AT
            val range =
                abs(fullAt - NORMAL_BODY_TEMPERATURE.value) - SPEED_PENALTY_DEAD_ZONE
            val progress =
                ((deviation - SPEED_PENALTY_DEAD_ZONE) / range).coerceIn(
                    0.0,
                    1.0
                )
            return SpeedPenalty(SPEED_PENALTY_MAX.value * progress)
        }
    }
}