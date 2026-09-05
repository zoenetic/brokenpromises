package dev.zoenetic.brokenpromises.environment

import dev.zoenetic.brokenpromises.vitals.tickVitals
import dev.zoenetic.brokenpromises.effects.player.tickMovementSpeedReduction
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

internal const val INTERVAL_TICKS = 20

public fun ServerLevel.tickEnvironment(tick: Long) {
    for (player in getPlayers(LivingEntity::isAlive)) {
        val _ = player.tickEnvironment(tick)
    }
    //TODO: Not doing anything with the exposure, yet
}

internal fun ServerPlayer.tickEnvironment(tick: Long): ConditionsSample {
    val cached = environmentalConditionsCache[uuid]
    var elapsed = 0L
    if (cached != null) {
        elapsed = tick - cached.tick
        if (elapsed < INTERVAL_TICKS) {
            return cached
        }
    }
    val climate = getClimate()
    val conditions = getConditions(climate)
    tickVitals(conditions, elapsed)
    tickMovementSpeedReduction()
    val sample = ConditionsSample(
        this.uuid,
        tick,
        conditions
    )
    environmentalConditionsCache[uuid] = sample
    return sample
}