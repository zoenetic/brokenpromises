package dev.zoenetic.brokenpromises.survival.state

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.units.Power
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.chunk.LevelChunk
import kotlin.math.ceil
import kotlin.math.sqrt

internal val MIN_HEAT_CONTRIBUTION = Celsius(0.1)
internal const val HEAT_SOURCE_SOFTENING = 1.0

public data class HeatSource(
    val position: BlockPos,
    val power: Power,
)

public val HEAT_SOURCE_BLOCKS: Map<Block, Power> by lazy {
    mapOf(
        Blocks.CAMPFIRE to Power(30.0),
        Blocks.CANDLE to Power(0.5),
        Blocks.FIRE to Power(40.0),
        Blocks.FURNACE to Power(20.0),
        Blocks.LAVA to Power(100.0),
        Blocks.MAGMA_BLOCK to Power(20.0),
        Blocks.TORCH to Power(3.0),
        Blocks.WALL_TORCH to Power(3.0),
    )
}

internal val MAX_HEAT_RADIUS: Int by lazy {
    val maxPower = HEAT_SOURCE_BLOCKS.values.maxOf { it.value }
    ceil(sqrt(maxPower / MIN_HEAT_CONTRIBUTION.value)).toInt()
}

public fun BlockState.isHeatSourceBlock(): Boolean {
    return HEAT_SOURCE_BLOCKS.containsKey(block)
}

public fun BlockState.isLit(): Boolean {
    return getValueOrElse(BlockStateProperties.LIT, true)
}

public typealias HeatSourceIndex = Long2ObjectOpenHashMap<Power>

public object ChunkHeatSources {
    public fun around(player: Player): List<HeatSource> {
        val level = player.level()
        val pos = player.blockPosition()
        val r = MAX_HEAT_RADIUS
        val minPos = getMinPos(level, pos, r)
        val maxPos = getMaxPos(level, pos, r)
        val minChunkX = SectionPos.blockToSectionCoord(minPos.x)
        val minChunkZ = SectionPos.blockToSectionCoord(minPos.z)
        val maxChunkX = SectionPos.blockToSectionCoord(maxPos.x)
        val maxChunkZ = SectionPos.blockToSectionCoord(maxPos.z)
        val sources = mutableListOf<HeatSource>()
        for (chunkX in minChunkX..maxChunkX) {
            for (chunkZ in minChunkZ..maxChunkZ) {
                val chunk = level.getChunk(chunkX, chunkZ)
                val chunkSources = Survival.platform.heatSources.get(chunk)
                if (chunkSources == null) {
                    rebuild(chunk)
                    continue
                }
                for ((pos, power) in chunkSources) {
                    val blockPos = BlockPos.of(pos)
                    val state = chunk.getBlockState(blockPos)
                    if (!state.isHeatSourceBlock() || !state.isLit()) continue
                    sources.add(HeatSource(blockPos, power))
                }
            }
        }
        return sources
    }

    public fun of(chunk: LevelChunk): HeatSourceIndex? {
        return Survival.platform.heatSources.get(chunk)
    }

    public fun rebuild(chunk: LevelChunk) {
        if (chunk.level.isClientSide) return
        val index = Survival.platform.heatSources.get(chunk) ?: HeatSourceIndex()
        for (sectionY in chunk.minSectionY..chunk.maxSectionY) {
            val section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY))
            if (section.hasOnlyAir()) continue
            if (!section.maybeHas(BlockState::isHeatSourceBlock)) continue
            val originX = chunk.pos.minBlockX
            val originY = SectionPos.sectionToBlockCoord(sectionY)
            val originZ = chunk.pos.minBlockZ
            for (localY in 0..15) {
                for (localZ in 0..15) {
                    for (localX in 0..15) {
                        val state = section.getBlockState(localX, localY, localZ)
                        if (!state.isHeatSourceBlock()) continue
                        val power = HEAT_SOURCE_BLOCKS[state.block] ?: continue
                        index.put(
                            BlockPos.asLong(
                                originX + localX,
                                originY + localY,
                                originZ + localZ,
                            ),
                            power,
                        )
                    }
                }
            }
        }
    }

    public fun onBlockChanged(chunk: LevelChunk, pos: BlockPos, state: BlockState) {
        if (!state.isHeatSourceBlock()) return removeHeatSource(chunk, pos)
        val index = Survival.platform.heatSources.get(chunk) ?: HeatSourceIndex()
        val power = HEAT_SOURCE_BLOCKS[state.block]
        if (power == null) {
            Survival.LOGGER.warn("no power found for heat source block: ${state.block}")
            return removeHeatSource(chunk, pos)
        }
        index.put(pos.asLong(), power)
    }

    public fun removeHeatSource(chunk: LevelChunk, pos: BlockPos) {
        Survival.platform.heatSources.get(chunk)?.remove(pos.asLong())
    }
}

internal fun getMinPos(level: Level, pos: BlockPos, r: Int): BlockPos {
    val minY = (pos.y - r).coerceAtLeast(level.minY)
    return BlockPos(pos.x - r, minY, pos.z - r)
}

internal fun getMaxPos(level: Level, pos: BlockPos, r: Int): BlockPos {
    val maxY = (pos.y + r).coerceAtMost(level.maxY)
    return BlockPos(pos.x + r, maxY, pos.z + r)
}