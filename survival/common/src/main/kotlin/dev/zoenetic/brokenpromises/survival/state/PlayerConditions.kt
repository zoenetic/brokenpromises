package dev.zoenetic.brokenpromises.survival.state

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.probe.*
import dev.zoenetic.brokenpromises.survival.units.*
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

private val INTERVAL = Duration(20L)

public class PlayerConditions(
    public val humidity: Humidity,
    public val isUnderOpenSky: Boolean,
    public val sky: Sky,
    public val temperature: Celsius,
    public val wind: Wind,
    public val time: Time,
) {
    public companion object {

        public fun get(player: ServerPlayer): PlayerConditions? =
            Survival.platform.playerConditions.get(player)

        public fun getNew(player: ServerPlayer, time: Time): PlayerConditions {
            val level = player.level()
            val pos = player.blockPosition()
            val chunk = level.getChunkAt(pos)
            val chunkClimate = chunk.getClimate()
            val heat = sumHeatSources(player.boundingBox.center, ChunkHeatSources.around(player))
            val sky = player.getSky()
            return PlayerConditions(
                chunkClimate.humidity,
                player.isUnderOpenSky(),
                sky,
                chunk.getTemperature(
                    Altitude(pos.y - level.seaLevel),
                    chunkClimate.humidity, sky, Time(level.overworldClockTime)
                ) + heat,
                chunk.getWind(),
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
            wind = Wind(Vec3.ZERO),
            time = Time(0L),
        )
    }
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