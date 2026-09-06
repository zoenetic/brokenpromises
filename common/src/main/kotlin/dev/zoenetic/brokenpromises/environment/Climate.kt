package dev.zoenetic.brokenpromises.environment

import dev.zoenetic.brokenpromises.heat.Temperature
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ColumnPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.DensityFunction

public data class ClimateSample(
    val humidity: Humidity,
    val temperature: Temperature,
    val wind: Wind,
    //TODO: Continentality
    //TODO: Ocean currents
)

private val cache: HashMap<ResourceKey<Level>, HashMap<ColumnPos, ClimateSample>> = HashMap()

public fun ServerPlayer.getClimate(): ClimateSample {
    val level = level()
    val dimension: ResourceKey<Level> = level.dimension()
    val pos = blockPosition()
    val column = ColumnPos(pos.x, pos.z)
    val cachedColumns = cache.getOrPut(dimension) { HashMap() }
    val cached = cachedColumns[column]
    if (cached != null) {
        return cached
    }
    val sampler = level.chunkSource.randomState().sampler()
    val context = DensityFunction.SinglePointContext(pos.x, pos.y, pos.z)
    val humidity = Humidity.fromNoise(sampler.humidity.compute(context))
    val temperature = Temperature.fromNoise(sampler.temperature.compute(context))
    val wind = Wind.fromClimateSampler(sampler, pos)
    val sample = ClimateSample(humidity, temperature, wind)
    cachedColumns[column] = sample
    return sample
}