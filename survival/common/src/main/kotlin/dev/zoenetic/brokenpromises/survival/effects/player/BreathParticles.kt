package dev.zoenetic.brokenpromises.survival.effects.player

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.units.Humidity
import dev.zoenetic.brokenpromises.survival.units.Time
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import java.util.*

public val BREATH_VISIBLE_BELOW: Celsius = Celsius(8.0)
public val HUMIDITY_TO_BUMP_BREATH_VISIBLE_AT: Humidity = Humidity(0.5)

public class BreathParticles() {

    public companion object {

        internal val cache: HashMap<UUID, Time> = HashMap()

        internal fun emitBreathParticle(player: ServerPlayer, temperature: Celsius) {
            if (temperature < BREATH_VISIBLE_BELOW) {
                val eye = player.eyePosition
                val look = player.getViewVector(1.0f)
                val mouth = eye.add(look.scale(0.25)).subtract(0.0, 0.15, 0.0)
                player.level().sendParticles(
                    ParticleTypes.WHITE_SMOKE,
                    mouth.x, mouth.y, mouth.z,
                    0,
                    look.x + 0.3, look.y + 0.1, look.z + 0.3,
                    0.005
                )
            }
        }

        public fun tick(level: ServerLevel) {
            val gameTime = level.gameTime
            val players = level.players()
            players.forEach { player -> tick(player, Time(gameTime)) }
        }

        public fun tick(player: ServerPlayer, gameTime: Time) {
            val vitals = Survival.platform.vitals.get(player) ?: return
            val breath = vitals.breathingRate.toBreath() ?: return
            val interval = breath.interval
            val conditions = Survival.platform.playerConditions.get(player) ?: return
            val last = cache.getOrPut(player.uuid) { gameTime }
            val elapsed = gameTime - last
            if (elapsed < interval) return
            emitBreathParticle(player, conditions.temperature)
            cache[player.uuid] = gameTime
        }
    }
}