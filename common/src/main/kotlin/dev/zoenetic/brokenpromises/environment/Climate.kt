package dev.zoenetic.brokenpromises.environment

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.DensityFunction

public data class Climate(
    val temperature: Temperature,
    val precipitation: Precipitation,
)

public fun ServerLevel.getClimate(pos: BlockPos): Climate {
    val s = chunkSource.randomState().sampler()
    val ctx = DensityFunction.SinglePointContext(pos.x, pos.y, pos.z)
    val t = Temperature.fromNoise(s.temperature.compute(ctx))
    val p = Precipitation.fromHumidityAndTemperature(s.humidity.compute(ctx), t)
    return Climate(t, p)
}