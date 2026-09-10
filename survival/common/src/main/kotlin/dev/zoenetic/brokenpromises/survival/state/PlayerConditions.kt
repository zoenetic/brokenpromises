package dev.zoenetic.brokenpromises.survival.state

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.probe.*
import dev.zoenetic.brokenpromises.survival.units.*
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

public class PlayerConditions(
    public val humidity: Humidity = Humidity(0.5),
    public val isUnderOpenSky: Boolean = true,
    public val sky: Sky = Sky(0.0),
    public val temperature: Celsius = Celsius(24.0),
    public val wind: Wind = Wind(Vec3.ZERO),
    public val time: Ticks = Ticks(0L),
) {
    public companion object {

        public fun latest(player: ServerPlayer): PlayerConditions {
            return Survival.platform.playerConditions.get(player)
        }

        public fun get(player: ServerPlayer): PlayerConditions {
            val level = player.level()
            val pos = player.blockPosition()
            val chunk = level.getChunkAt(pos)
            val time = Ticks(level.gameTime)
            val clock = Ticks(level.overworldClockTime)
            val chunkClimate = chunk.getClimate()
            val sky = player.getSky()
            val temperature = chunk.getTemperature(
                Altitude(pos.y - level.seaLevel),
                chunkClimate.humidity, sky, clock
            )
            return PlayerConditions(
                chunkClimate.humidity,
                player.isUnderOpenSky(),
                sky,
                temperature,
                chunk.getWind(),
                time,
            )
        }

        public fun set(player: ServerPlayer, conditions: PlayerConditions) {
            Survival.platform.playerConditions.set(player, conditions)
        }

        public fun tick(level: Level) {
            if (level !is ServerLevel) return
            val players = level.players()
            players.forEach { player ->
                tick(player)
            }
        }

        public fun tick(player: ServerPlayer) {
            val conditions = get(player)
            set(player, conditions)
        }
    }
}

internal fun sumHeatSources(body: Vec3, sources: MutableList<HeatSource>): Double {
    var heat = 0.0
    for ((position, power) in sources) {
        val distanceSq = body.distanceToSqr(Vec3.atCenterOf(position))
        val sq = distanceSq + HEAT_SOURCE_SOFTENING
        heat += power.value / sq
    }
    return heat
}