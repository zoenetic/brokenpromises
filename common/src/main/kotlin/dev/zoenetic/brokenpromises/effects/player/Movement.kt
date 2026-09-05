package dev.zoenetic.brokenpromises.effects.player

import dev.zoenetic.brokenpromises.BrokenPromises.MOD_ID
import dev.zoenetic.brokenpromises.vitals.NORMAL_BODY_TEMPERATURE
import dev.zoenetic.brokenpromises.vitals.getVitals
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import kotlin.math.abs

internal const val SPEED_PENALTY_DEAD_ZONE = 1.0
internal const val SPEED_PENALTY_MAX = 0.6

internal const val COLD_FULL_PENALTY_AT = 28.0
internal const val HEAT_FULL_PENALTY_AT = 41.0

internal fun speedPenalty(bodyTemperature: Double): Double {
    val deviation =
        abs(bodyTemperature - NORMAL_BODY_TEMPERATURE)
    if (deviation <= SPEED_PENALTY_DEAD_ZONE) return 0.0
    val fullAt =
        if (bodyTemperature < NORMAL_BODY_TEMPERATURE) COLD_FULL_PENALTY_AT else HEAT_FULL_PENALTY_AT
    val range =
        abs(fullAt - NORMAL_BODY_TEMPERATURE) - SPEED_PENALTY_DEAD_ZONE
    val progress =
        ((deviation - SPEED_PENALTY_DEAD_ZONE) / range).coerceIn(
            0.0,
            1.0
        )
    return SPEED_PENALTY_MAX * progress
}

private val BODY_TEMPERATURE_SPEED_REDUCTION =
    Identifier.fromNamespaceAndPath(
        MOD_ID,
        "body_temperature_speed_reduction"
    )

public fun ServerPlayer.tickMovementSpeedReduction() {
    val penalty =
        speedPenalty(getVitals().temperature.value)
    val attribute = getAttribute(MOVEMENT_SPEED) ?: return
    if (penalty == 0.0) {
        attribute.removeModifier(
            BODY_TEMPERATURE_SPEED_REDUCTION
        )
    } else {
        attribute.addOrUpdateTransientModifier(
            AttributeModifier(
                BODY_TEMPERATURE_SPEED_REDUCTION,
                -penalty,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )
        )
    }
}