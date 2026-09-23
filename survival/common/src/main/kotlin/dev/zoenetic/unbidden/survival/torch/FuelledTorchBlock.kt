package dev.zoenetic.unbidden.survival.torch

import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.unbidden.survival.emission.EmittingBlock
import dev.zoenetic.unbidden.survival.emission.LightTable
import dev.zoenetic.unbidden.survival.fuel.Burnout
import dev.zoenetic.unbidden.survival.fuel.Fuel
import dev.zoenetic.unbidden.survival.fuel.FuelProperties.FUEL_LEVEL
import dev.zoenetic.unbidden.survival.fuel.FuelledBlock
import dev.zoenetic.unbidden.survival.units.Duration
import dev.zoenetic.unbidden.survival.units.Heat
import dev.zoenetic.unbidden.survival.units.Light
import dev.zoenetic.unbidden.survival.units.Time
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.TorchBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT
import net.minecraft.world.level.levelgen.SurfaceRules.state

public open class FuelledTorchBlock(
    flameParticle: SimpleParticleType,
    properties: Properties,
) : TorchBlock(flameParticle, properties), EmittingBlock, FuelledBlock {

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(LIT, false)
                .setValue(FUEL_LEVEL, 15)
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

    override val lightTable: LightTable = LightTable.IDENTITY

    override val maxFuel: Fuel
        get() = Fuel(15)

    override val maxLight: Light get() = Light(lightTable[maxFuel.level])

    override val burnRate: Duration get() = Duration(200L)

    internal val props: Properties get() = properties
    internal val flame: SimpleParticleType get() = flameParticle

    override val maxHeat: Heat = Heat(6.0) //TODO: fix
    override fun getHeat(state: BlockState): Heat =
        if (state.getValue(LIT)) maxHeat else Heat(0.0)

    override fun getLight(state: BlockState): Light {
        if (!state.getValue(LIT)) return Light.NONE
        return Light(lightTable[getFuel(state).level])
    }

    override fun getFuel(state: BlockState): Fuel = Fuel(state.getValue(FUEL_LEVEL))

    override fun setFuel(state: BlockState, fuel: Fuel): BlockState {
        val newValue = fuel.coerceAtMost(maxFuel)
        return state.setValue(FUEL_LEVEL, newValue.level)
    }

    override fun getBurnout(existingBurnout: Time?, now: Time, fuel: Fuel): Burnout = Burnout.forFuel(existingBurnout, now, fuel, maxFuel, burnRate)

    override fun exhausted(state: BlockState): BlockState = state.setValue(LIT, false)

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