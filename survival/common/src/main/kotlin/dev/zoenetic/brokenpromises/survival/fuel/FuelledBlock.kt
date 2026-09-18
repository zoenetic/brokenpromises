package dev.zoenetic.brokenpromises.survival.fuel

import net.minecraft.world.level.block.state.BlockState

public interface FuelledBlock {
    public val maxFuel: Fuel
    public fun getFuel(state: BlockState): Fuel
    public fun setFuel(state: BlockState, fuel: Fuel): BlockState
}
