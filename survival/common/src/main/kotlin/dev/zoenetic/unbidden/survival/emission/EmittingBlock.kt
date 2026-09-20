package dev.zoenetic.unbidden.survival.emission

import dev.zoenetic.unbidden.survival.units.Heat
import net.minecraft.world.level.block.state.BlockState

public interface EmittingBlock {

    public fun getHeat(state: BlockState): Heat
    public val maxHeat: Heat
        get() = TODO()

    public fun getLight(state: BlockState): Light
    public val maxLight: Light
        get() = TODO()

    public companion object {
        public fun simple(heat: Heat, light: Light): EmittingBlock = object : EmittingBlock {
            override fun getHeat(state: BlockState): Heat = maxHeat
            override val maxHeat: Heat = heat

            override fun getLight(state: BlockState): Light = maxLight
            override val maxLight: Light = light
        }
    }
}

