package dev.zoenetic.brokenpromises.environment

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import java.util.UUID

internal const val INTERVAL_TICKS = 20

public val exposureCache: HashMap<UUID, ExposureSample> = hashMapOf<UUID, ExposureSample>()

public fun ServerLevel.tickEnvironment(tick: Long) {
    for (player in getPlayers(LivingEntity::isAlive)) {
        player.tickEnvironment(tick)
    }
    //TODO: Not doing anything with the exposure, yet
}

internal fun ServerPlayer.tickEnvironment(tick: Long): Exposure {
    val cached = exposureCache[uuid]
    if (cached != null) {
        val elapsed = tick - cached.tick
        if (elapsed < INTERVAL_TICKS) {
            return cached.exposure
        }
    }
    val climate = getClimate()
    val conditions = getConditions(climate)
    val exposure = getExposure(conditions)
    val sample = ExposureSample(
        this.uuid,
        tick,
        exposure
    )
    exposureCache[uuid] = sample
    return sample.exposure
}