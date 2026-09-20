package dev.zoenetic.brokenpromises.survival.campfire

import dev.zoenetic.brokenpromises.survival.emission.EmittingBlock
import dev.zoenetic.brokenpromises.survival.emission.Light
import dev.zoenetic.brokenpromises.survival.fuel.Fuel
import dev.zoenetic.brokenpromises.survival.fuel.FuelProperties.FUEL_LEVEL
import dev.zoenetic.brokenpromises.survival.fuel.FuelledBlock
import dev.zoenetic.brokenpromises.survival.units.Duration
import dev.zoenetic.brokenpromises.survival.units.Heat
import net.minecraft.world.level.block.state.BlockState

internal object BPCampfireBlock : FuelledBlock, EmittingBlock {
    override val burnRate: Duration = Duration(400L)
    override val maxFuel: Fuel = Fuel(15)

    override fun getFuel(state: BlockState): Fuel = Fuel(state.getValue(FUEL_LEVEL))
    override fun setFuel(state: BlockState, fuel: Fuel): BlockState =
        state.setValue(FUEL_LEVEL, fuel.coerceAtMost(maxFuel).level)

    override fun getHeat(state: BlockState): Heat = TODO()
    override fun getLight(state: BlockState): Light = TODO()
}