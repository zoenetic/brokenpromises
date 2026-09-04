package dev.zoenetic.brokenpromises.environment

import dev.zoenetic.brokenpromises.heat.HEAT_SOURCE_BLOCKS
import dev.zoenetic.brokenpromises.heat.Power
import dev.zoenetic.brokenpromises.heat.isHeatSourceBlock
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import java.util.UUID
import java.util.WeakHashMap

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
