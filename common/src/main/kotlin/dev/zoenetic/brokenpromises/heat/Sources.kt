package dev.zoenetic.brokenpromises.heat

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
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

public val globalHeatSourceState: WeakHashMap<Level, Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Power>>> =
    WeakHashMap()

internal const val MIN_HEAT_DISTANCE_SQ = 0.25
internal const val MIN_HEAT_CONTRIBUTION = 0.1

public data class HeatSource(
    val position: BlockPos,
    val power: Power,
)

public val HEAT_SOURCE_BLOCKS: Map<Block, Power> by lazy {
    mapOf(
        Blocks.CAMPFIRE to Power(30.0),
        Blocks.CANDLE to Power(0.5),
        Blocks.FIRE to Power(30.0),
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

public fun Level.getOrPutHeatSourceState(): Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<Power>> {
    return globalHeatSourceState.getOrPut(this) { Long2ObjectOpenHashMap() }
}

public fun LevelChunk.getOrPutHeatSourceState(): Long2ObjectOpenHashMap<Power> {
    val levelSources = level.getOrPutHeatSourceState()
    return levelSources.getOrPut(this.pos.pack()) { Long2ObjectOpenHashMap() }
}

public fun LevelChunk.rebuildHeatSourceState() {
    val levelSources = level.getOrPutHeatSourceState()
    val chunkSources = getHeatSources()
    levelSources.put(pos.pack(), chunkSources)
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
                    chunkSources.put(BlockPos.asLong(originX + localX, originY + localY, originZ + localZ), power)
                }
            }
        }
    }
    return chunkSources
}

public fun LevelChunk.dropHeatSourceState() {
    val levelSources = globalHeatSourceState[level] ?: return
    levelSources.remove(pos.pack())
}

public fun LevelChunk.updateStateForSingleHeatSource(pos: BlockPos, newState: BlockState) {
    val power = HEAT_SOURCE_BLOCKS[newState.block] ?: return dropStateForSingleHeatSource(pos)
    val source = HeatSource(pos, power)
    getOrPutStateForSingleHeatSource(source)
}

public fun LevelChunk.getOrPutStateForSingleHeatSource(source: HeatSource) {
    val (pos, power) = source
    val chunkSources = getOrPutHeatSourceState()
    chunkSources.put(pos.asLong(), power)
}

public fun LevelChunk.dropStateForSingleHeatSource(blockPos: BlockPos) {
    val levelSources = globalHeatSourceState[level] ?: return
    val chunkSources = levelSources[pos.pack()] ?: return
    chunkSources.remove(blockPos.asLong())
}

internal fun sumHeatSources(body: Vec3, sources: List<HeatSource>): Double {
    var heat = 0.0
    for ((position, power) in sources) {
        val distanceSq = body.distanceToSqr(Vec3.atCenterOf(position))
        heat += power.value / distanceSq.coerceAtLeast(MIN_HEAT_DISTANCE_SQ)
    }
    return heat
}