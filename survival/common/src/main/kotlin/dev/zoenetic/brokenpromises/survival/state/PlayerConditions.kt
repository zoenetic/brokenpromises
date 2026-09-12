package dev.zoenetic.brokenpromises.survival.state

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.probe.*
import dev.zoenetic.brokenpromises.survival.units.*
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.exp

private val INTERVAL = Duration(20L)

internal const val HEAT_TRAPPING: Double = 2.0
internal val MAX_HEATED_AIR: Celsius = Celsius(30.0)

public class PlayerConditions(
    public val humidity: Humidity,
    public val isUnderOpenSky: Boolean,
    public val sky: Sky,
    public val temperature: Celsius,
    public val heat: TemperatureDifference,
    public val wind: Wind,
    public val windExposure: Double,
    public val time: Time,
) {
    public fun feelsLike(): Celsius {
        val h = humidity.value
        val t = temperature.value
        val e = h * 6.105 * exp(17.27 * t / (237.7 + t))
        val w = wind.speed
        return Celsius(t + 0.33 * e - 0.70 * w - 4.00)
    }

    public companion object {

        public fun get(player: ServerPlayer): PlayerConditions? =
            Survival.platform.playerConditions.get(player)

        public fun getNew(player: ServerPlayer, time: Time): PlayerConditions {
            val level = player.level()
            val pos = player.blockPosition()
            val chunk = level.getChunkAt(pos)
            val chunkClimate = chunk.getClimate()
            val sky = player.getSky()
            val ambient = chunk.getTemperature(
                Altitude(pos.y - level.seaLevel),
                chunkClimate.humidity, sky, Time(level.overworldClockTime)
            )
            val radiant = sumHeatSources(player.boundingBox.center, ChunkHeatSources.around(player))
            val heat = trapHeat(radiant, sky, ambient)
            val chunkWind = chunk.getWind()
            val windExposure = player.getWindExposure(chunkWind)
            val wind = chunkWind.sheltered(windExposure)
            return PlayerConditions(
                chunkClimate.humidity,
                player.isUnderOpenSky(),
                sky,
                ambient + heat,
                heat,
                wind,
                windExposure,
                time
            )
        }

        public fun set(player: ServerPlayer, conditions: PlayerConditions) {
            Survival.platform.playerConditions.set(player, conditions)
        }

        public fun tick(level: Level) {
            if (level !is ServerLevel) return
            val time = Time(level.gameTime)
            level.players().forEach { player -> tick(player, time) }
        }

        public fun tick(player: ServerPlayer, time: Time) {
            val previous = get(player)
            if (previous != null) {
                val elapsed = time - previous.time
                if (elapsed < INTERVAL) return
            }
            val conditions = getNew(player, time)
            set(player, conditions)
        }

        public val EMPTY: PlayerConditions = PlayerConditions(
            humidity = Humidity(0.5),
            isUnderOpenSky = false,
            sky = Sky(1.0),
            temperature = Celsius(20.0),
            heat = TemperatureDifference(0.0),
            wind = CALM,
            windExposure = 1.0,
            time = Time(0L),
        )
    }
}

internal fun trapHeat(
    radiant: TemperatureDifference,
    sky: Sky,
    ambient: Celsius
): TemperatureDifference {
    val enclosure = (1.0 - sky.value).coerceIn(0.0, 1.0)
    val headroom = (MAX_HEATED_AIR.value - ambient.value - radiant.value).coerceAtLeast(0.0)
    val trapped = (radiant.value * HEAT_TRAPPING * enclosure).coerceAtMost(headroom)
    return TemperatureDifference(radiant.value + trapped)
}

internal fun sumHeatSources(body: Vec3, sources: List<HeatSource>): TemperatureDifference {
    var heat = 0.0
    for ((position, power) in sources) {
        val distanceSq = body.distanceToSqr(Vec3.atCenterOf(position))
        val sq = distanceSq + HEAT_SOURCE_SOFTENING
        heat += power.value / sq
    }
    return TemperatureDifference(heat)
}