package dev.zoenetic.brokenpromises.survival.block

import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.brokenpromises.survival.registry.BrokenPromisesItems
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.ItemTags
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.TorchBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.BlockHitResult

public open class FuelledTorchBlock(
    flameParticle: SimpleParticleType,
    properties: Properties,
) : TorchBlock(flameParticle, properties), FuelledLightSource {

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(LIT, false)
                .setValue(FUEL_LEVEL, FUEL_LEVEL.possibleValues.max())
        )
    }

    public override fun codec(): MapCodec<out FuelledTorchBlock> = CODEC

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (!state.getValue(LIT)) return
        super.animateTick(state, level, pos, random)
    }

    protected override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(LIT).add(FUEL_LEVEL)
    }

    override fun onPlace(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        oldState: BlockState,
        movedByPiston: Boolean
    ) {
    } //TODO: set expiryAt and fuel level

    protected override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (stack.`is`(BrokenPromisesItems.FUELLED_TORCH_ITEM) &&
            !state.getValue(LIT) && state.getValue(FUEL_LEVEL) > 0
        ) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LIT, true), UPDATE_ALL)
                level.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 1F, 1F)
            }
            return InteractionResult.SUCCESS
        }
        if (stack.`is`(ItemTags.COALS) && state.getValue(FUEL_LEVEL) < MAX_FUEL) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(FUEL_LEVEL, MAX_FUEL), UPDATE_ALL)
                stack.consume(1, player)
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1F, 1F)
            }
            return InteractionResult.SUCCESS
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
    }

    internal val props: Properties get() = properties
    internal val flame: SimpleParticleType get() = flameParticle

    public companion object {

        public val PARTICLE_OPTIONS_FIELD: MapCodec<SimpleParticleType> =
            BuiltInRegistries.PARTICLE_TYPE.byNameCodec().comapFlatMap(
                { type ->
                    if (type is SimpleParticleType) DataResult.success(type)
                    else DataResult.error { "Not a SimpleParticleType: $type" }
                },
                { type -> type }).fieldOf("particle_options")

        public val CODEC: MapCodec<FuelledTorchBlock> =
            RecordCodecBuilder.mapCodec { i: RecordCodecBuilder.Instance<FuelledTorchBlock> ->
                i.group(
                    PARTICLE_OPTIONS_FIELD.forGetter { b -> b.flame },
                    Properties.CODEC.fieldOf("properties").forGetter { b -> b.props }
                ).apply(i, ::FuelledTorchBlock)
            }
    }
}