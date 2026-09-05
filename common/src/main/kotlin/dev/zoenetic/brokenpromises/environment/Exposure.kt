package dev.zoenetic.brokenpromises.environment

import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.heat.getInMedium
import dev.zoenetic.brokenpromises.heat.getOnSurface
import dev.zoenetic.brokenpromises.vitals.BodyTemperature
import dev.zoenetic.brokenpromises.vitals.Vitals
import net.minecraft.SharedConstants
import net.minecraft.server.level.ServerPlayer
import kotlin.math.pow

public const val BODY_COOLS_AT: Double = 30.0 * 60.0 // 30 minutes
public const val BODY_WARMS_AT: Double = 60.0 // 1 minute

public fun ServerPlayer.tickVitals(conditions: Conditions, elapsed: Long): Vitals {
    val currentVitals = getVitals()
    if (currentVitals.temperature.value == conditions.temperature.value) return currentVitals
    val isWarming = currentVitals.temperature.value < conditions.temperature.value
    val inMedium = getInMedium()?.conductance ?: 1.0
    val onSurface = getOnSurface()?.conductance ?: 1.0
    val conductance = inMedium * onSurface
    val halfLifeSeconds = if (isWarming) BODY_WARMS_AT / conductance else BODY_COOLS_AT / conductance
    val newBodyTemperature = BodyTemperature(
        approach(
            currentVitals.temperature.value,
            conditions.temperature.value,
            elapsed,
            halfLifeSeconds
        )
    )
    val newVitals = currentVitals.copy(temperature = newBodyTemperature)
    BrokenPromises.platform.setVitals(this, newVitals)
    return newVitals
}

public fun ServerPlayer.getVitals(): Vitals {
    return BrokenPromises.platform.vitals(this)
}

internal fun approach(current: Double, target: Double, elapsedTicks: Long, halfLifeSeconds: Double): Double {
    val elapsedSeconds = elapsedTicks / SharedConstants.TICKS_PER_SECOND.toDouble()
    val remainingFraction = 0.5.pow(elapsedSeconds / halfLifeSeconds)
    return target + (current - target) * remainingFraction
}