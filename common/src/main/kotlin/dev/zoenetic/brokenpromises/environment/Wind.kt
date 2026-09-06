package dev.zoenetic.brokenpromises.environment

import net.minecraft.core.BlockPos
import net.minecraft.world.level.biome.Climate.Sampler
import net.minecraft.world.level.levelgen.DensityFunction
import net.minecraft.world.phys.Vec3

public data class Wind(public val vector: Vec3) {

    val speed: Double get() = vector.length()
    val conductance: Double get() = 1.0 + CHILL * speed

    public companion object {

        public val CALM: Wind = Wind(Vec3(0.0, 0.0, 0.0))

        public const val GRADIENT_STEP: Int = 16
        public const val SPEED_SCALE: Double = 1000.0
        public const val MAX_SPEED: Double = 15.0
        public const val CHILL: Double = 0.2

        public fun fromClimateSampler(sampler: Sampler, pos: BlockPos): Wind {
            val function = sampler.temperature
            val gradient =
                function.horizontalGradient(pos.x, pos.y, pos.z, GRADIENT_STEP)   // tens of blocks
            val slope = gradient.horizontalDistance()
            if (slope < 1e-9) return CALM
            val speed = (slope * SPEED_SCALE).coerceAtMost(MAX_SPEED)
            return Wind(gradient.normalize().scale(speed))
        }
    }
}

public fun DensityFunction.horizontalGradient(x: Int, y: Int, z: Int, h: Int): Vec3 {
    val dx = compute(DensityFunction.SinglePointContext(x + h, y, z)) -
            compute(DensityFunction.SinglePointContext(x - h, y, z))
    val dz = compute(DensityFunction.SinglePointContext(x, y, z + h)) -
            compute(DensityFunction.SinglePointContext(x, y, z - h))
    return Vec3(dx / (2.0 * h), 0.0, dz / (2.0 * h))
}