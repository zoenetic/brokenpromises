package dev.zoenetic.unbidden.survival.emission

import dev.zoenetic.unbidden.survival.Survival
import dev.zoenetic.unbidden.survival.units.Heat
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt

public val EMITTERS: TagKey<Block> = TagKey.create(
    Registries.BLOCK,
    Identifier.fromNamespaceAndPath(Survival.NAMESPACE, "emitters")
)

internal val MIN_HEAT_CONTRIBUTION = Heat(0.1)

public object Emitters {

    internal const val EMISSION_SOFTENING = 1.0
    internal const val LIGHT_RADIUS: Int = 15

    internal val HEAT_RADIUS: Int
        get() = VANILLA_EMITTERS.values.maxOf { sqrt(it.maxHeat.celsius / MIN_HEAT_CONTRIBUTION.celsius).toInt() }

    internal fun at(level: ServerLevel, pos: BlockPos, radius: Int): List<BlockPos> {
        val minPos = getMinPos(level, pos, radius)
        val maxPos = getMaxPos(level, pos, radius)
        val minChunkX = SectionPos.blockToSectionCoord(minPos.x)
        val minChunkZ = SectionPos.blockToSectionCoord(minPos.z)
        val maxChunkX = SectionPos.blockToSectionCoord(maxPos.x)
        val maxChunkZ = SectionPos.blockToSectionCoord(maxPos.z)
        val emitters = mutableListOf<BlockPos>()
        for (chunkX in minChunkX..maxChunkX) {
            for (chunkZ in minChunkZ..maxChunkZ) {
                val chunk = level.getChunk(chunkX, chunkZ)
                val chunkEmitters = Survival.platform.emitters.get(chunk)
                for ((pos) in chunkEmitters) {
                    val blockPos = BlockPos.of(pos)
                    val state = chunk.getBlockState(blockPos)
                    if (!state.isEmittingBlock()) continue
                    emitters.add(blockPos)
                }
            }
        }
        return emitters
    }

    public fun atPlayer(player: ServerPlayer): List<BlockPos> {
        val level = player.level()
        val pos = player.blockPosition()
        return at(level, pos, HEAT_RADIUS)
    }

    internal fun getMaxPos(level: ServerLevel, pos: BlockPos, radius: Int): BlockPos {
        val maxY = (pos.y + radius).coerceAtMost(level.maxY)
        return BlockPos(pos.x + radius, maxY, pos.z + radius)
    }

    internal fun getMinPos(level: ServerLevel, pos: BlockPos, radius: Int): BlockPos {
        val minY = (pos.y - radius).coerceAtLeast(level.minY)
        return BlockPos(pos.x - radius, minY, pos.z - radius)
    }

    public fun heatAtPlayer(player: ServerPlayer): Heat {
        val bodyPos = player.boundingBox.center
        val blockPos = player.blockPosition()
        val level = player.level()
        val emitters = at(level, blockPos, HEAT_RADIUS)
        var total = Heat(0.0)
        for (pos in emitters) {
            val state = level.getBlockState(pos)
            if (!state.isLit()) continue
            val power = state.heat() ?: continue
            total += heatFrom(bodyPos, pos, power)
        }
        return total
    }

    internal fun heatFrom(body: Vec3, source: BlockPos, heat: Heat): Heat {
        val distanceSq = body.distanceToSqr(Vec3.atCenterOf(source))
        return Heat(heat.celsius / (distanceSq + EMISSION_SOFTENING))
    }

    public fun BlockState.isEmittingBlock(): Boolean {
        return block is EmittingBlock
    }

    public fun BlockState.isLit(): Boolean {
        return getValueOrElse(BlockStateProperties.LIT, true)
    }

    internal fun BlockState.heat(): Heat? {
        val block = block
        return if (block is EmittingBlock) block.getHeat(this) else null
    }

    // mixin hook: vanilla caches each state's light level when the state is built
    @JvmStatic
    public fun lightEmission(state: BlockState): Int {
        val block = state.block
        if (block !is EmittingBlock) return 0
        return block.getLight(state).value
    }

    public fun LevelChunk.rebuildEmitters() {
        if (level.isClientSide) return
        val index = Survival.platform.emitters.get(this)
        val time = level.gameTime
        for (sectionY in minSectionY..maxSectionY) {
            val section = getSection(getSectionIndexFromSectionY(sectionY))
            if (section.hasOnlyAir()) continue
            if (!section.maybeHas { state -> state.isEmittingBlock() }) continue
            val originX = pos.minBlockX
            val originY = SectionPos.sectionToBlockCoord(sectionY)
            val originZ = pos.minBlockZ
            for (localY in 0..15) {
                for (localZ in 0..15) {
                    for (localX in 0..15) {
                        val state = section.getBlockState(localX, localY, localZ)
                        if (!state.isEmittingBlock()) continue
                        index.put(
                            BlockPos.asLong(
                                originX + localX,
                                originY + localY,
                                originZ + localZ
                            ), time
                        )
                    }
                }
            }
        }
    }

    public fun lightAtPlayer(level: ServerLevel, player: ServerPlayer): Light {
        val blockPos = player.blockPosition()
        val emitters = at(level, blockPos, LIGHT_RADIUS)
        var brightest = Light.NONE
        for (pos in emitters) {
            val state = level.getBlockState(pos)
            val block = state.block
            if (block !is EmittingBlock) continue
            val light = block.getLight(state)
            if (light > brightest) brightest = light
        }
        return brightest
    }

    @JvmStatic
    public fun onBlockChanged(
        chunk: LevelChunk,
        pos: BlockPos,
        oldState: BlockState,
        state: BlockState
    ) {
        val time = chunk.level.gameTime
        if (!state.isEmittingBlock()) return remove(chunk, pos)
        val index = Survival.platform.emitters.get(chunk)
        index.put(pos.asLong(), time)
    }

    public fun remove(chunk: LevelChunk, pos: BlockPos) {
        Survival.platform.emitters.get(chunk).remove(pos.asLong())
    }

}