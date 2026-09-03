package dev.zoenetic.brokenpromises.environment

import dev.zoenetic.brokenpromises.heat.HEAT_SOURCE_BLOCKS
import dev.zoenetic.brokenpromises.heat.HeatSource
import dev.zoenetic.brokenpromises.heat.MAX_BLOCKS_HEAT_RADIATES
import dev.zoenetic.brokenpromises.heat.MIN_HEAT_DISTANCE_SQ
import dev.zoenetic.brokenpromises.heat.Temperature
import dev.zoenetic.brokenpromises.heat.adjustTemperatureForAltitude
import dev.zoenetic.brokenpromises.heat.isHeatSourceBlock
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import java.util.stream.Stream

public data class Conditions(
    val temperature: Temperature,
    val precipitation: Precipitation,
)

public fun ServerPlayer.getConditions(climate: ClimateSample): Conditions {
    val pos = blockPosition()
    val altitude = pos.y - level().seaLevel
    val precipitation = climate.precipitation
    val temperature = adjustTemperatureForAltitude(climate.temperature, altitude)
    val level = level()
    val r = MAX_BLOCKS_HEAT_RADIATES
    val minPos = getMinPos(level, pos, r)
    val maxPos = getMaxPos(level, pos, r)
    val sectionPositions = sectionPositionsInHeatRadius(minPos, maxPos)
    val body = boundingBox.center
    val sources = mutableListOf<HeatSource>()
    for (sectionPos in sectionPositions) {
        val chunk = level.chunkSource.getChunkNow(sectionPos.x, sectionPos.z) ?: continue
        val index = chunk.getSectionIndexFromSectionY(sectionPos.y)
        val section = chunk.getSection(index)
        if (section.hasOnlyAir()) continue
        if (!section.maybeHas(BlockState::isHeatSourceBlock)) continue
        val minX = maxOf(sectionPos.minBlockX(), minPos.x)
        val minY = maxOf(sectionPos.minBlockY(), minPos.y)
        val minZ = maxOf(sectionPos.minBlockZ(), minPos.z)
        val maxX = minOf(sectionPos.maxBlockX(), maxPos.x)
        val maxY = minOf(sectionPos.maxBlockY(), maxPos.y)
        val maxZ = minOf(sectionPos.maxBlockZ(), maxPos.z)
        for (blockPos in BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            val state = section.getBlockState(
                blockPos.x - sectionPos.minBlockX(),
                blockPos.y - sectionPos.minBlockY(),
                blockPos.z - sectionPos.minBlockZ(),
            )
            if (!state.isHeatSourceBlock()) continue
            val power = HEAT_SOURCE_BLOCKS[state.block] ?: continue
            // TODO: occlusion
            // TODO: shelter
            // TODO: wind
            // TODO: bigger radiuses/caching
            sources.add(HeatSource(blockPos, power))
        }
    }
    val heat = sumHeatSources(body, sources.toList())
    return Conditions(Temperature(temperature.value + heat), precipitation)
}

internal fun getMinPos(level: ServerLevel, pos: BlockPos, r: Int): BlockPos {
    val minY = (pos.y - r).coerceAtLeast(level.minY)
    return BlockPos(pos.x - r, minY, pos.z - r)
}

internal fun getMaxPos(level: ServerLevel, pos: BlockPos, r: Int): BlockPos {
    val maxY = (pos.y + r).coerceAtMost(level.maxY)
    return BlockPos(pos.x + r, maxY, pos.z + r)
}

internal fun sectionPositionsInHeatRadius(minPos: BlockPos, maxPos: BlockPos): Stream<SectionPos> {
    val minSection = SectionPos.of(minPos)
    val maxSection = SectionPos.of(maxPos)
    return SectionPos.betweenClosedStream(
        minSection.x, minSection.y, minSection.z,
        maxSection.x, maxSection.y, maxSection.z,
    )
}

internal fun sumHeatSources(body: Vec3, sources: List<HeatSource>): Double {
    var heat = 0.0
    for ((position, power) in sources) {
        val distanceSq = body.distanceToSqr(Vec3.atCenterOf(position))
        heat += power.value / distanceSq.coerceAtLeast(MIN_HEAT_DISTANCE_SQ)
    }
    return heat
}
