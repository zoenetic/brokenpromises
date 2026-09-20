package dev.zoenetic.unbidden.survival.fuel

import dev.zoenetic.unbidden.survival.units.Duration
import net.minecraft.world.level.block.state.BlockState

public interface FuelledBlock {
    public val burnRate: Duration
    public val maxFuel: Fuel
    public fun getFuel(state: BlockState): Fuel
    public fun setFuel(state: BlockState, fuel: Fuel): BlockState
}
