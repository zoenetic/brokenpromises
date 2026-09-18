package dev.zoenetic.brokenpromises.survival.torch

import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.brokenpromises.survival.emission.EmittingBlock
import dev.zoenetic.brokenpromises.survival.emission.Light
import dev.zoenetic.brokenpromises.survival.fuel.Fuel
import dev.zoenetic.brokenpromises.survival.fuel.FuelProperties.FUEL_LEVEL
import dev.zoenetic.brokenpromises.survival.fuel.FuelledBlock
import dev.zoenetic.brokenpromises.survival.registry.BrokenPromisesItems.FUELLED_TORCH_ITEM
import dev.zoenetic.brokenpromises.survival.units.Power
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT
import net.minecraft.world.phys.BlockHitResult
import kotlin.math.ceil

public open class FuelledTorchBlock(
    flameParticle: SimpleParticleType,
    properties: Properties,
) : TorchBlock(flameParticle, properties), EmittingBlock, FuelledBlock {

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(LIT, false)
                .setValue(FUEL_LEVEL, maxFuel.level)
        )
    }

    public override fun codec(): MapCodec<out FuelledTorchBlock> = CODEC

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        if (!state.getValue(LIT)) return
        super.animateTick(state, level, pos, random)
    }

    protected override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder
            .add(LIT)
            .add(FUEL_LEVEL)
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
        if (stack.`is`(FUELLED_TORCH_ITEM) &&
            !state.getValue(LIT) && state.getValue(FUEL_LEVEL) > 0
        ) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LIT, true), UPDATE_ALL)
                level.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 1F, 1F)
            }
            return InteractionResult.SUCCESS
        }
        if (stack.`is`(ItemTags.COALS) && getFuel(state) < maxFuel) {
            if (!level.isClientSide) {
                level.setBlock(pos, setFuel(state, maxFuel), UPDATE_ALL)
                stack.consume(1, player)
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1F, 1F)
            }
            return InteractionResult.SUCCESS
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
    }

    internal val props: Properties get() = properties
    internal val flame: SimpleParticleType get() = flameParticle

    // a getter, not a field: this is read while the states are built, before fields are set
    override val maxFuel: Fuel get() = Fuel.MAX

    override fun getHeat(state: BlockState): Power =
        if (state.getValue(LIT)) HEAT else Power(0.0)

    override fun getLight(state: BlockState): Light {
        if (!state.getValue(LIT)) return Light.NONE
        val fraction = getFuel(state).level / maxFuel.level.toDouble()
        return Light(ceil(Light.MAX.value * fraction).toInt())
    }

    override fun getFuel(state: BlockState): Fuel = Fuel(state.getValue(FUEL_LEVEL))

    override fun setFuel(state: BlockState, fuel: Fuel): BlockState =
        state.setValue(FUEL_LEVEL, fuel.coerceAtMost(maxFuel).level)

    public companion object {

        public val HEAT: Power = Power(3.0)

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