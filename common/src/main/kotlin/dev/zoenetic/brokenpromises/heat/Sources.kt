package dev.zoenetic.brokenpromises.heat

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

internal const val MIN_HEAT_DISTANCE_SQ = 1.0

public data class HeatSource(
    val position: BlockPos,
    val power: Power,
)

public val HEAT_SOURCE_BLOCKS: Map<Block, Power> by lazy { mapOf(
        Blocks.CAMPFIRE to Power(25.0),
        Blocks.CANDLE to Power(0.5),
        Blocks.FURNACE to Power(20.0),
        Blocks.LAVA to Power(60.0),
        Blocks.TORCH to Power(2.0),
        Blocks.WALL_TORCH to Power(2.0),
    )}

public fun BlockState.isHeatSourceBlock(): Boolean {
    return HEAT_SOURCE_BLOCKS.containsKey(this.block)
            && getValueOrElse(BlockStateProperties.LIT, true)
}