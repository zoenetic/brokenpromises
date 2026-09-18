package dev.zoenetic.brokenpromises.survival.emission

import dev.zoenetic.brokenpromises.survival.units.Power
import net.minecraft.world.level.block.state.BlockState

// called while block states are being built, so implementations must read only the state
public interface EmittingBlock {

    public fun getHeat(state: BlockState): Power
    public fun getLight(state: BlockState): Light

}
