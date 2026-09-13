package dev.zoenetic.brokenpromises.survival.block

import com.mojang.serialization.MapCodec
import dev.zoenetic.brokenpromises.survival.registry.Items
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Direction.Axis
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.tags.FluidTags.WATER
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.*
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

public class FirewoodBlock(properties: Properties) : Block(properties), SimpleWaterloggedBlock {

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(WATERLOGGED, false)
                .setValue(AXIS, Axis.Z)
        )
    }

    override fun codec(): MapCodec<FirewoodBlock> = CODEC

    override fun useWithoutItem(
        state: BlockState, level: Level, pos: BlockPos,
        player: Player, hit: BlockHitResult
    ): InteractionResult {
        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty) {
            return takeOne(level, pos, state, player)
        }
        return InteractionResult.PASS
    }

    internal fun takeOne(
        level: Level,
        pos: BlockPos,
        state: BlockState,
        player: Player
    ): InteractionResult {
        val billets = state.getValue(BILLETS)
        if (billets > 1) {
            level.setBlock(pos, state.setValue(BILLETS, billets - 1), 3)
        } else {
            level.setBlock(
                pos,
                state.fluidState.createLegacyBlock(),
                3
            )
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos)
        }
        val taken = ItemStack(Items.FIREWOOD_ITEM, 1)
        level.playSound(player, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1F, 1F)
        if (!player.inventory.add(taken)) player.drop(taken, false)
        return InteractionResult.SUCCESS
    }

    override fun canBeReplaced(state: BlockState, context: BlockPlaceContext): Boolean {
        return (!context.isSecondaryUseActive && context.itemInHand
            .item == this.asItem()
                && state.getValue(BILLETS) < 12) || super.canBeReplaced(state, context)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? {
        val state = context.level.getBlockState(context.clickedPos)
        if (state.`is`(this)) return state.cycle(BILLETS)
        val replacedFluidState = context.level.getFluidState(context.clickedPos)
        val isWaterSource = replacedFluidState.`is`(Fluids.WATER)
        return super.getStateForPlacement(context)
            ?.setValue(AXIS, context.horizontalDirection.axis)
            ?.setValue(WATERLOGGED, isWaterSource)
    }

    override fun updateShape(
        state: BlockState,
        level: LevelReader,
        ticks: ScheduledTickAccess,
        pos: BlockPos,
        directionToNeighbour: Direction,
        neighbourPos: BlockPos,
        neighbourState: BlockState,
        random: RandomSource
    ): BlockState {
        if (state.getValue(WATERLOGGED)) ticks.scheduleTick(
            pos,
            Fluids.WATER,
            Fluids.WATER.getTickDelay(level)
        )
        return super.updateShape(
            state,
            level,
            ticks,
            pos,
            directionToNeighbour,
            neighbourPos,
            neighbourState,
            random
        )
    }

    override fun getFluidState(state: BlockState): FluidState {
        return if (state.getValue(WATERLOGGED)) Fluids.WATER.getSource(false)
        else super.getFluidState(state)
    }

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        return shapeFor(state.getValue(BILLETS), state.getValue(AXIS))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(AXIS, BILLETS, WATERLOGGED)
    }

    override fun placeLiquid(
        level: LevelAccessor,
        pos: BlockPos,
        state: BlockState,
        fluidState: FluidState
    ): Boolean {
        if (!state.getValue(WATERLOGGED) && fluidState.`is`(WATER)) {
            val newState = state.setValue(WATERLOGGED, true)
            level.setBlock(pos, newState, 3)
            level.scheduleTick(pos, fluidState.type, fluidState.type.getTickDelay(level))
            return true
        }
        return false
    }

    public companion object {
        public val CODEC: MapCodec<FirewoodBlock> = simpleCodec(::FirewoodBlock)
        public val BILLETS: IntegerProperty =
            IntegerProperty.create("billets", MIN_BILLETS, MAX_BILLETS)
        public const val MIN_BILLETS: Int = 1
        public const val MAX_BILLETS: Int = 12
        public val AXIS: EnumProperty<Axis> = BlockStateProperties.HORIZONTAL_AXIS
        public val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED

        private val SHAPES_Z: Array<VoxelShape> = arrayOf(
            box(6.0, 0.0, 0.0, 10.0, 4.0, 16.0),
            Shapes.or(
                box(2.0, 0.0, 0.0, 6.0, 4.0, 16.0),
                box(10.0, 0.0, 0.0, 14.0, 4.0, 16.0)
            ),
            Shapes.or(
                box(1.0, 0.0, 0.0, 5.0, 4.0, 16.0),
                box(11.0, 0.0, 0.0, 15.0, 4.0, 16.0),
                box(6.0, 0.0, 0.0, 10.0, 4.0, 16.0)
            ),
            Shapes.or(
                box(1.0, 0.0, 0.0, 5.0, 4.0, 16.0),
                box(11.0, 0.0, 0.0, 15.0, 4.0, 16.0),
                box(6.0, 0.0, 0.0, 10.0, 4.0, 16.0),
                box(0.0, 4.0, 6.0, 16.0, 8.0, 10.0)
            ),
            Shapes.or(
                box(1.0, 0.0, 0.0, 5.0, 4.0, 16.0),
                box(11.0, 0.0, 0.0, 15.0, 4.0, 16.0),
                box(6.0, 0.0, 0.0, 10.0, 4.0, 16.0),
                box(0.0, 4.0, 2.0, 16.0, 8.0, 6.0),
                box(0.0, 4.0, 10.0, 16.0, 8.0, 14.0)
            ),
            Shapes.or(
                box(1.0, 0.0, 0.0, 5.0, 4.0, 16.0),
                box(11.0, 0.0, 0.0, 15.0, 4.0, 16.0),
                box(6.0, 0.0, 0.0, 10.0, 4.0, 16.0),
                box(0.0, 4.0, 1.0, 16.0, 8.0, 5.0),
                box(0.0, 4.0, 6.0, 16.0, 8.0, 10.0),
                box(0.0, 4.0, 11.0, 16.0, 8.0, 15.0)
            ),
            Shapes.or(
                box(1.0, 0.0, 0.0, 5.0, 4.0, 16.0),
                box(11.0, 0.0, 0.0, 15.0, 4.0, 16.0),
                box(6.0, 0.0, 0.0, 10.0, 4.0, 16.0),
                box(0.0, 4.0, 1.0, 16.0, 8.0, 5.0),
                box(0.0, 4.0, 6.0, 16.0, 8.0, 10.0),
                box(0.0, 4.0, 11.0, 16.0, 8.0, 15.0),
                box(6.0, 8.0, 0.0, 10.0, 12.0, 16.0)
            ),
            Shapes.or(
                box(1.0, 0.0, 0.0, 5.0, 4.0, 16.0),
                box(11.0, 0.0, 0.0, 15.0, 4.0, 16.0),
                box(6.0, 0.0, 0.0, 10.0, 4.0, 16.0),
                box(0.0, 4.0, 1.0, 16.0, 8.0, 5.0),
                box(0.0, 4.0, 6.0, 16.0, 8.0, 10.0),
                box(0.0, 4.0, 11.0, 16.0, 8.0, 15.0),
                box(2.0, 8.0, 0.0, 6.0, 12.0, 16.0),
                box(10.0, 8.0, 0.0, 14.0, 12.0, 16.0)
            ),
            Shapes.or(
                box(1.0, 0.0, 0.0, 5.0, 4.0, 16.0),
                box(11.0, 0.0, 0.0, 15.0, 4.0, 16.0),
                box(6.0, 0.0, 0.0, 10.0, 4.0, 16.0),
                box(0.0, 4.0, 1.0, 16.0, 8.0, 5.0),
                box(0.0, 4.0, 6.0, 16.0, 8.0, 10.0),
                box(0.0, 4.0, 11.0, 16.0, 8.0, 15.0),
                box(1.0, 8.0, 0.0, 5.0, 12.0, 16.0),
                box(6.0, 8.0, 0.0, 10.0, 12.0, 16.0),
                box(11.0, 8.0, 0.0, 15.0, 12.0, 16.0)
            ),
            Shapes.or(
                box(1.0, 0.0, 0.0, 5.0, 4.0, 16.0),
                box(11.0, 0.0, 0.0, 15.0, 4.0, 16.0),
                box(6.0, 0.0, 0.0, 10.0, 4.0, 16.0),
                box(0.0, 4.0, 1.0, 16.0, 8.0, 5.0),
                box(0.0, 4.0, 6.0, 16.0, 8.0, 10.0),
                box(0.0, 4.0, 11.0, 16.0, 8.0, 15.0),
                box(1.0, 8.0, 0.0, 5.0, 12.0, 16.0),
                box(6.0, 8.0, 0.0, 10.0, 12.0, 16.0),
                box(11.0, 8.0, 0.0, 15.0, 12.0, 16.0),
                box(0.0, 12.0, 6.0, 16.0, 16.0, 10.0)
            ),
            Shapes.or(
                box(1.0, 0.0, 0.0, 5.0, 4.0, 16.0),
                box(11.0, 0.0, 0.0, 15.0, 4.0, 16.0),
                box(6.0, 0.0, 0.0, 10.0, 4.0, 16.0),
                box(0.0, 4.0, 1.0, 16.0, 8.0, 5.0),
                box(0.0, 4.0, 6.0, 16.0, 8.0, 10.0),
                box(0.0, 4.0, 11.0, 16.0, 8.0, 15.0),
                box(1.0, 8.0, 0.0, 5.0, 12.0, 16.0),
                box(6.0, 8.0, 0.0, 10.0, 12.0, 16.0),
                box(11.0, 8.0, 0.0, 15.0, 12.0, 16.0),
                box(0.0, 12.0, 10.0, 16.0, 16.0, 14.0),
                box(0.0, 12.0, 2.0, 16.0, 16.0, 6.0)
            ),
            Shapes.or(
                box(1.0, 0.0, 0.0, 5.0, 4.0, 16.0),
                box(11.0, 0.0, 0.0, 15.0, 4.0, 16.0),
                box(6.0, 0.0, 0.0, 10.0, 4.0, 16.0),
                box(0.0, 4.0, 1.0, 16.0, 8.0, 5.0),
                box(0.0, 4.0, 6.0, 16.0, 8.0, 10.0),
                box(0.0, 4.0, 11.0, 16.0, 8.0, 15.0),
                box(1.0, 8.0, 0.0, 5.0, 12.0, 16.0),
                box(6.0, 8.0, 0.0, 10.0, 12.0, 16.0),
                box(11.0, 8.0, 0.0, 15.0, 12.0, 16.0),
                box(0.0, 12.0, 11.0, 16.0, 16.0, 15.0),
                box(0.0, 12.0, 1.0, 16.0, 16.0, 5.0),
                box(0.0, 12.0, 6.0, 16.0, 16.0, 10.0)
            )
        )

        private val SHAPES: List<Map<Axis, VoxelShape>> =
            SHAPES_Z.map { Shapes.rotateHorizontalAxis(it) }

        public fun shapeFor(billets: Int, axis: Axis): VoxelShape =
            SHAPES[billets - MIN_BILLETS].getValue(axis)

        @JvmStatic
        public fun maybeSplit(context: UseOnContext): Boolean {
            val level: Level = context.level
            val pos: BlockPos = context.clickedPos
            val player: Player = context.player ?: return false
            val state = level.getBlockState(pos)
            if (state.canBeSplit()) {
                if (context.clickedFace.axis === state.getValue(BlockStateProperties.AXIS)) {
                    if (level.isClientSide) return true
                    level.destroyBlock(pos, false, player, 512)
                    val firewood = ItemStack(Items.FIREWOOD_ITEM.value(), 4)
                    popResource(level, pos, firewood)
                    val axe = context.itemInHand
                    axe.hurtAndBreak(1, player, context.hand)
                    return true
                }
            }
            return false
        }
    }
}

public fun BlockState.canBeSplit(): Boolean {
    return this.`is`(BlockTags.LOGS)
}