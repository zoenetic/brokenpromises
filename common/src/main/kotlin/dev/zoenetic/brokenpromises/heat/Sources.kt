package dev.zoenetic.brokenpromises.heat

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.ceil
import kotlin.math.sqrt

internal const val MIN_HEAT_CONTRIBUTION = 0.1
internal const val HEAT_SOURCE_SOFTENING = 1.0

public class HeatSources(
    public val cache: WeakHashMap<Level, Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Power>>>
) {
    public fun getOrPutLevel(level: Level): Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Power>> {
        return cache.getOrPut(level) { Long2ObjectOpenHashMap() }
    }

    public fun getOrPutChunk(level: Level, pos: ChunkPos): Long2ObjectOpenHashMap<Power> {
        val level = getOrPutLevel(level)
        return level.getOrPut(pos.pack()) { Long2ObjectOpenHashMap() }
    }

    public fun rebuildChunk(chunk: LevelChunk) {
        val level = getOrPutLevel(chunk.level)
        val sources = chunk.getHeatSources()
        level.put(chunk.pos.pack(), sources)
    }

    public fun dropChunk(chunk: LevelChunk) {
        val level = chunk.level
        val sources = cache[level] ?: return
        sources.remove(chunk.pos.pack())
    }

    public fun updateOne(chunk: LevelChunk, pos: BlockPos, newState: BlockState) {
        val power = HEAT_SOURCE_BLOCKS[newState.block] ?: return this.dropOne(chunk, pos)
        val source = HeatSource(pos, power)
        getOrPutOne(chunk, source)
    }

    public fun getOrPutOne(chunk: LevelChunk, source: HeatSource) {
        val level = chunk.level
        val pos = source.position
        val power = source.power
        val chunkSources = getOrPutChunk(level, chunk.pos)
        chunkSources.put(pos.asLong(), power)
    }

    public fun dropOne(chunk: LevelChunk, blockPos: BlockPos) {
        val levelSources = cache[chunk.level] ?: return
        val chunkSources = levelSources[chunk.pos.pack()] ?: return
        chunkSources.remove(blockPos.asLong())
    }

    public fun tick() {
        TODO()
    }

    public companion object {
        public fun new(): HeatSources {
            return HeatSources(
                WeakHashMap<Level, Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Power>>>()
            )
        }
    }
}

public class HeatSource(
    public val position: BlockPos,
    public val power: Power,
) {
    public companion object {
        public fun fromBlock(pos: BlockPos, block: Block): HeatSource? {
            val power = HEAT_SOURCE_BLOCKS[block]
            return if (power !== null) HeatSource(
                pos,
                power,
            ) else null
        }
    }
}

private val HEAT_SOURCE_BLOCKS: Map<Block, Power> by lazy {
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
    ceil(sqrt(maxPower / MIN_HEAT_CONTRIBUTION)).toInt()
}

public fun BlockState.isHeatSourceBlock(): Boolean {
    return HEAT_SOURCE_BLOCKS.containsKey(block)
}

public fun BlockState.isLit(): Boolean {
    return getValueOrElse(BlockStateProperties.LIT, true)
}

internal fun LevelChunk.getHeatSources(): Long2ObjectOpenHashMap<Power> {
    val chunkSources = Long2ObjectOpenHashMap<Power>()
    for (sectionY in minSectionY..maxSectionY) {
        val section = getSection(getSectionIndexFromSectionY(sectionY))
        if (section.hasOnlyAir()) continue
        if (!section.maybeHas(BlockState::isHeatSourceBlock)) continue
        val originX = pos.minBlockX
        val originY = SectionPos.sectionToBlockCoord(sectionY)
        val originZ = pos.minBlockZ
        for (localY in 0..15) {
            for (localZ in 0..15) {
                for (localX in 0..15) {
                    val state = section.getBlockState(localX, localY, localZ)
                    if (!state.isHeatSourceBlock()) continue
                    val power = HEAT_SOURCE_BLOCKS[state.block] ?: continue
                    chunkSources.put(
                        BlockPos.asLong(
                            originX + localX,
                            originY + localY,
                            originZ + localZ
                        ), power
                    )
                }
            }
        }
    }
    return chunkSources
}

internal fun sumHeatSources(body: Vec3, sources: List<HeatSource>): Double {
    var heat = 0.0
    for (source in sources) {
        val pos = source.position
        val power = source.power
        val distanceSq = body.distanceToSqr(Vec3.atCenterOf(pos))
        val sq = distanceSq + HEAT_SOURCE_SOFTENING
        heat += power.value / sq
    }
    return heat
}