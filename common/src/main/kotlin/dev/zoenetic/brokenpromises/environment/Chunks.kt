package dev.zoenetic.brokenpromises.environment

import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.heat.*
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ColumnPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.DensityFunction

public class ChunkConditions(
    public val humidity: Humidity,
    public val shelter: Shelter,
    public val temperature: Temperature,
    public val wind: Wind,
)

public class BrokenPromisesChunks(
    public val conditions: HashMap<ResourceKey<Level>, HashMap<ColumnPos, ChunkConditions>>,
    public val heatSources: HeatSources
) {
    public fun atPlayer(player: ServerPlayer): ChunkConditions {
        val level = player.level()
        val levelCache = conditions.getOrPut(level.dimension()) { HashMap() }
        val pos = player.blockPosition()
        val column = ColumnPos(pos.x, pos.z)
        val conditions = getConditions(level, pos)
        val time = level.overworldClockTime
        val altitude = pos.y - level.seaLevel
        val shelter = Shelter.get(level, pos)
        val sky = shelter.sky
        val humidity = conditions.humidity
        val temperature = conditions.temperature.adjust(altitude, time, sky, humidity)
        val wind = conditions.wind
        val r = MAX_HEAT_RADIUS
        val minPos = getMinPos(level, pos, r)
        val maxPos = getMaxPos(level, pos, r)
        val minChunkX = SectionPos.blockToSectionCoord(minPos.x)
        val minChunkZ = SectionPos.blockToSectionCoord(minPos.z)
        val maxChunkX = SectionPos.blockToSectionCoord(maxPos.x)
        val maxChunkZ = SectionPos.blockToSectionCoord(maxPos.z)
        val body = player.boundingBox.center
        val sources = mutableListOf<HeatSource>()
        for (chunkX in minChunkX..maxChunkX) {
            for (chunkZ in minChunkZ..maxChunkZ) {
                val chunkSources = heatSources.getOrPutChunk(level, ChunkPos(chunkX, chunkZ))
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
        if (BrokenPromises.platform.isDevelopmentEnvironment)
            BrokenPromises.LOGGER.info(
                "conditions: base={} sources={} heat={}",
                temperature.value,
                sources.size,
                heat
            )
        val chunkConditions = ChunkConditions(
            humidity,
            shelter,
            Temperature(temperature.value + heat),
            wind
        )
        levelCache[column] = chunkConditions
        return chunkConditions
    }

    public fun getConditions(level: ServerLevel, pos: BlockPos): ChunkConditions {
        val sampler = level.chunkSource.randomState().sampler()
        val context = DensityFunction.SinglePointContext(pos.x, pos.y, pos.z)
        val humidity = Humidity.fromNoise(sampler.humidity.compute(context))
        val shelter = Shelter.get(level, pos)
        val temperature = Temperature.fromNoise(sampler.temperature.compute(context))
        val wind = Wind.fromClimateSampler(sampler, pos)
        val sample = ChunkConditions(
            humidity,
            shelter,
            temperature,
            wind
        )
        return sample
    }

    public companion object {
        public fun new(): BrokenPromisesChunks {
            return BrokenPromisesChunks(
                conditions = hashMapOf(),
                heatSources = HeatSources.new()
            )
        }
    }
}

internal fun getMinPos(level: ServerLevel, pos: BlockPos, r: Int): BlockPos {
    val minY = (pos.y - r).coerceAtLeast(level.minY)
    return BlockPos(pos.x - r, minY, pos.z - r)
}

internal fun getMaxPos(level: ServerLevel, pos: BlockPos, r: Int): BlockPos {
    val maxY = (pos.y + r).coerceAtMost(level.maxY)
    return BlockPos(pos.x + r, maxY, pos.z + r)
}