package dev.zoenetic.brokenpromises.effects.player

import dev.zoenetic.brokenpromises.BrokenPromises.Companion.MOD_ID
import dev.zoenetic.brokenpromises.vitals.BodyTemperature
import dev.zoenetic.brokenpromises.vitals.NORMAL_BODY_TEMPERATURE
import net.minecraft.resources.Identifier
import kotlin.math.abs

internal const val SPEED_PENALTY_DEAD_ZONE = 1.0
internal const val SPEED_PENALTY_MAX = 0.6
internal const val COLD_FULL_PENALTY_AT = 28.0
internal const val HEAT_FULL_PENALTY_AT = 41.0

internal fun speedPenalty(bodyTemperature: BodyTemperature): Double {
    val temperature = bodyTemperature.value
    val deviation =
        abs(temperature - NORMAL_BODY_TEMPERATURE)
    if (deviation <= SPEED_PENALTY_DEAD_ZONE) return 0.0
    val fullAt =
        if (temperature < NORMAL_BODY_TEMPERATURE) COLD_FULL_PENALTY_AT else HEAT_FULL_PENALTY_AT
    val range =
        abs(fullAt - NORMAL_BODY_TEMPERATURE) - SPEED_PENALTY_DEAD_ZONE
    val progress =
        ((deviation - SPEED_PENALTY_DEAD_ZONE) / range).coerceIn(
            0.0,
            1.0
        )
    return SPEED_PENALTY_MAX * progress
}

public val BODY_TEMPERATURE_SPEED_REDUCTION: Identifier =
    Identifier.fromNamespaceAndPath(
        MOD_ID,
        "body_temperature_speed_reduction"
    )