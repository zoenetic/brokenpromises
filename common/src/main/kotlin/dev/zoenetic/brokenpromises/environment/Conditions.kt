package dev.zoenetic.brokenpromises.environment

import dev.zoenetic.brokenpromises.heat.HeatSource
import dev.zoenetic.brokenpromises.heat.MAX_HEAT_RADIUS
import dev.zoenetic.brokenpromises.heat.MIN_HEAT_DISTANCE_SQ
import dev.zoenetic.brokenpromises.heat.Temperature
import dev.zoenetic.brokenpromises.heat.adjustTemperatureForAltitude
import dev.zoenetic.brokenpromises.heat.globalHeatSourceState
import dev.zoenetic.brokenpromises.heat.isHeatSourceBlock
import dev.zoenetic.brokenpromises.heat.isLit
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec3
import java.util.UUID

public data class Conditions(
    val precipitation: Precipitation,
    val temperature: Temperature,
)

public data class ConditionsSample(
    val player: UUID,
    val tick: Long,
    val conditions: Conditions,
)

public val environmentalConditionsCache: HashMap<UUID, ConditionsSample> = hashMapOf<UUID, ConditionsSample>()

public fun ServerPlayer.getConditions(climate: ClimateSample): Conditions {
    val level = level()
    val pos = blockPosition()
    val altitude = pos.y - level().seaLevel
    val precipitation = climate.precipitation
    val temperature = adjustTemperatureForAltitude(climate.temperature, altitude)
    val levelState = globalHeatSourceState[level] ?: return Conditions(precipitation, temperature)
    val r = MAX_HEAT_RADIUS
    val minPos = getMinPos(level, pos, r)
    val maxPos = getMaxPos(level, pos, r)
    val minChunkX = SectionPos.blockToSectionCoord(minPos.x)
    val minChunkZ = SectionPos.blockToSectionCoord(minPos.z)
    val maxChunkX = SectionPos.blockToSectionCoord(maxPos.x)
    val maxChunkZ = SectionPos.blockToSectionCoord(maxPos.z)
    val body = boundingBox.center
    val sources = mutableListOf<HeatSource>()
    for (chunkX in minChunkX..maxChunkX) {
        for (chunkZ in minChunkZ..maxChunkZ) {
            val chunkSources = levelState[ChunkPos.pack(chunkX, chunkZ)] ?: continue
            val chunk = level.chunkSource.getChunkNow(chunkX, chunkZ) ?: continue
            for ((packed, power) in chunkSources) {
                val blockPos = BlockPos.of(packed)
                val state = chunk.getBlockState(blockPos)
                if (!state.isHeatSourceBlock() || !state.isLit()) continue
                sources.add(HeatSource(blockPos, power))
            }
        }
    }
    val heat = sumHeatSources(body, sources.toList())
    return Conditions(precipitation, Temperature(temperature.value + heat))
}

internal fun getMinPos(level: ServerLevel, pos: BlockPos, r: Int): BlockPos {
    val minY = (pos.y - r).coerceAtLeast(level.minY)
    return BlockPos(pos.x - r, minY, pos.z - r)
}

internal fun getMaxPos(level: ServerLevel, pos: BlockPos, r: Int): BlockPos {
    val maxY = (pos.y + r).coerceAtMost(level.maxY)
    return BlockPos(pos.x + r, maxY, pos.z + r)
}

internal fun sumHeatSources(body: Vec3, sources: List<HeatSource>): Double {
    var heat = 0.0
    for ((position, power) in sources) {
        val distanceSq = body.distanceToSqr(Vec3.atCenterOf(position))
        heat += power.value / distanceSq.coerceAtLeast(MIN_HEAT_DISTANCE_SQ)
    }
    return heat
}
