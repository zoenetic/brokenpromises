package dev.zoenetic.brokenpromises.survival.probe

import dev.zoenetic.brokenpromises.survival.sim.ClimateSample
import dev.zoenetic.brokenpromises.survival.units.*
import net.minecraft.SharedConstants
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.DensityFunction
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

internal const val DIURNAL_SWING_DRY = 11.0
internal const val DIURNAL_SWING_HUMID = 3.5
internal const val WARMEST_TICK = 9000L // 15:00
internal const val LAPSE_RATE_PER_BLOCK = 0.07
internal const val EQUATOR_C: Double = 30.0
internal const val POLE_C: Double = -20.0
internal const val LATITUDE_FALLOFF = 1.3

public fun LevelChunk.getClimate(): ClimateSample {
    val humidity = getHumidity()
    val temperature = getTemperature()
    val wind = getWind()
    return ClimateSample(
        humidity,
        temperature,
        wind,
    )
}

public fun LevelChunk.getHumidity(): Humidity {
    val level = level as ServerLevel
    val function = level.chunkSource.randomState().sampler().humidity
    return Humidity.fromNoise(
        compute(function)
    )
}

public fun LevelChunk.getTemperature(): Celsius = getBaseTemperature()

public fun LevelChunk.getTemperature(
    altitude: Altitude? = null,
    humidity: Humidity,
    sky: Sky,
    time: Time,
): Celsius {
    return getBaseTemperature()
        .adjustForAltitude(altitude)
        .adjustForTimeOfDay(time, sky, humidity)
}

public fun LevelChunk.getWind(): Wind {
    val level = level as ServerLevel
    val function = level.chunkSource.randomState().sampler().temperature
    return Wind.fromDensityFunction(
        function,
        this,
    )
}

private fun LevelChunk.compute(function: DensityFunction): Double {
    val context = DensityFunction.SinglePointContext(
        pos.middleBlockX,
        level.seaLevel,
        pos.middleBlockZ,
    )
    return function.compute(context)
}

internal fun LevelChunk.getBaseTemperature(): Celsius {
    val level = level as ServerLevel
    val function = level.chunkSource.randomState().sampler().temperature
    return temperatureFromNoise(compute(function))
}

/** Maps climate noise onto a latitude band running from [POLE_C] to [EQUATOR_C]. */
internal fun temperatureFromNoise(noise: Double): Celsius {
    val latitude = (PI / 4.0) * (1.0 - noise.coerceIn(-1.0, 1.0))
    return Celsius(POLE_C + (EQUATOR_C - POLE_C) * cos(latitude).pow(LATITUDE_FALLOFF))
}

internal fun Celsius.adjustForAltitude(
    altitude: Altitude? = null,
): Celsius {
    if (altitude == null) return this
    val t = if (altitude.value > 0) {
        value - (altitude.value * LAPSE_RATE_PER_BLOCK)
    } else {
        value
    }
    return Celsius(t)
}

internal fun Celsius.adjustForTimeOfDay(
    time: Time,
    sky: Sky,
    humidity: Humidity
): Celsius {
    val dayFraction = (time.value - WARMEST_TICK).toDouble() / SharedConstants.TICKS_PER_GAME_DAY
    val swing = Mth.lerp(humidity.value, DIURNAL_SWING_DRY, DIURNAL_SWING_HUMID) * sky.value
    return Celsius(value + swing * cos(2.0 * PI * dayFraction))
}