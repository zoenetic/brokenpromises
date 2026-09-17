package dev.zoenetic.brokenpromises.survival.block

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import kotlin.math.ceil

public interface FuelledLightSource {

    public val LIT: BooleanProperty
        get() = BlockStateProperties.LIT

    public val FUEL_LEVEL: IntegerProperty
        get() = SurvivalBlockProperties.FUEL_LEVEL

    public val MAX_FUEL: Int
        get() = SurvivalBlockProperties.MAX_FUEL

    public fun lightLevel(state: BlockState): Int = FuelledLight.levelOf(state)
}

public object FuelledLight {

    public const val MAX_LIGHT: Int = 15

    public val BY_FUEL: List<Int> = (0..SurvivalBlockProperties.MAX_FUEL).map { fuel ->
        ceil(MAX_LIGHT * fuel / SurvivalBlockProperties.MAX_FUEL.toDouble()).toInt()
    }

    public fun levelOf(state: BlockState): Int =
        if (state.getValue(BlockStateProperties.LIT)) {
            BY_FUEL[state.getValue(SurvivalBlockProperties.FUEL_LEVEL)]
        } else {
            0
        }
}
