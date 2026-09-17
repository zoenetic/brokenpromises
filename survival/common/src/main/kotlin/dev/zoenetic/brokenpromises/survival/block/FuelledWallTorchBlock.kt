package dev.zoenetic.brokenpromises.survival.block

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.util.RandomSource
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ScheduledTickAccess
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.WallTorchBlock
import net.minecraft.world.level.block.WallTorchBlock.FACING
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

public class FuelledWallTorchBlock(
    flameParticle: SimpleParticleType,
    properties: Properties,
) : FuelledTorchBlock(flameParticle, properties) {

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(FUEL_LEVEL, FUEL_LEVEL.possibleValues.max())
        )
    }

    // vanilla's own animateTick places the particles against the wall for us
    public override fun animateTick(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        random: RandomSource
    ) {
        if (!state.getValue(LIT)) return
        Blocks.WALL_TORCH.animateTick(state, level, pos, random)
    }

    public override fun canSurvive(
        state: BlockState,
        level: LevelReader,
        pos: BlockPos
    ): Boolean = WallTorchBlock.canSurvive(level, pos, state.getValue(FACING))

    public override fun codec(): MapCodec<FuelledWallTorchBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(FACING)
    }

    public override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape = WallTorchBlock.getShape(state)

    public override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        for (direction in context.nearestLookingDirections) {
            if (!direction.axis.isHorizontal) continue
            val state = defaultBlockState().setValue(FACING, direction.opposite)
            if (state.canSurvive(context.level, context.clickedPos)) return state
        }
        return null
    }

    protected override fun updateShape(
        state: BlockState,
        level: LevelReader,
        ticks: ScheduledTickAccess,
        pos: BlockPos,
        directionToNeighbour: Direction,
        neighbourPos: BlockPos,
        neighbourState: BlockState,
        random: RandomSource
    ): BlockState {
        return if (directionToNeighbour.opposite == state.getValue(FACING) && !state.canSurvive(
                level,
                pos
            )
        ) {
            Blocks.AIR.defaultBlockState()
        } else {
            state
        }
    }

    public companion object {
        public val CODEC: MapCodec<FuelledWallTorchBlock> =
            RecordCodecBuilder.mapCodec { i: RecordCodecBuilder.Instance<FuelledWallTorchBlock> ->
                i.group(
                    PARTICLE_OPTIONS_FIELD.forGetter { b -> b.flame },
                    Properties.CODEC.fieldOf("properties").forGetter { b -> b.props }
                ).apply(i, ::FuelledWallTorchBlock)
            }
    }

}