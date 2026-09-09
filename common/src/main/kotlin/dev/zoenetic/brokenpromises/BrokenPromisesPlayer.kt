package dev.zoenetic.brokenpromises

import dev.zoenetic.brokenpromises.effects.player.BODY_TEMPERATURE_SPEED_REDUCTION
import dev.zoenetic.brokenpromises.effects.player.speedPenalty
import dev.zoenetic.brokenpromises.environment.ChunkConditions
import dev.zoenetic.brokenpromises.heat.ConductiveMedium
import dev.zoenetic.brokenpromises.heat.ConductiveSurface
import dev.zoenetic.brokenpromises.heat.METAL
import dev.zoenetic.brokenpromises.vitals.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Blocks
import java.util.*

public class BrokenPromisesPlayers(
    public val map: HashMap<UUID, BrokenPromisesPlayer>
) {

    public fun tick(conditions: ChunkConditions, elapsed: Long) {
        map.values.forEach { player ->
            player.tickVitals(conditions, elapsed)
            player.tickEffects(conditions, elapsed)
        }
    }

    public companion object {
        public fun new(): BrokenPromisesPlayers = BrokenPromisesPlayers(
            hashMapOf(),
        )
    }
}

public class BrokenPromisesPlayer(
    private val player: Player,
    private val movementStatistics: MovementStatistics,
) {
    private fun getNewBodyTemperature(
        conditions: ChunkConditions,
        elapsed: Long,
    ): BodyTemperature {
        val target = targetTemperature(conditions.temperature.value)
        val current = vitals().getBodyTemperature()
        if (current.value == target) return current
        val isWarming = current.value < target
        val inMedium = getInConductiveMedium()?.conductance
        val onSurface = getOnConductiveSurface()?.conductance
        val fromWind = conditions.wind.conductance
        val halfLifeSeconds =
            effectiveHalfLifeForBodyTemperature(isWarming, inMedium, onSurface, fromWind)
        return BodyTemperature(
            approachBodyTemperature(
                current.value,
                target,
                elapsed,
                halfLifeSeconds
            )
        )
    }

    public fun setBodyTemperature(temperature: BodyTemperature) {
        val vitals = BrokenPromises.platform.vitals(player)
        vitals.setBodyTemperature(temperature)
        BrokenPromises.platform.setVitals(player, vitals)
    }

    private fun getNewHeartRate(conditions: ChunkConditions, elapsed: Long): HeartRate {
        TODO()
    }

    public fun getInConductiveMedium(): ConductiveMedium? {
        if (player.isInLava) return ConductiveMedium.LAVA
        if (player.isInWater) return ConductiveMedium.WATER
        if (player.isInPowderSnow) return ConductiveMedium.POWDER_SNOW
        if (player.isInWaterOrRain) return ConductiveMedium.RAIN
        return null
    }

    public fun getOnConductiveSurface(): ConductiveSurface? {
        val on = player.blockStateOn
        if (on.`is`(Blocks.MAGMA_BLOCK)) return ConductiveSurface.MAGMA
        if (on.`is`(BlockTags.SNOW)) return ConductiveSurface.SNOW
        if (on.`is`(BlockTags.BASE_STONE_OVERWORLD)) return ConductiveSurface.STONE
        if (on.`is`(METAL)) return ConductiveSurface.METAL
        if (on.`is`(BlockTags.ICE)) return ConductiveSurface.ICE
        return null
    }

    public fun getNewVitals(conditions: ChunkConditions, elapsed: Long): Vitals {
        val bodyTemperature = getNewBodyTemperature(conditions, elapsed)
        val heartRate = getNewHeartRate(conditions, elapsed)
        return Vitals(
            bodyTemperature,
            heartRate,
        )
    }

    public fun vitals(): Vitals {
        return BrokenPromises.platform.vitals(player)
    }

    internal fun tickVitals(
        conditions: ChunkConditions,
        elapsed: Long
    ) {
        val vitals = getNewVitals(conditions, elapsed)
        BrokenPromises.platform.setVitals(player, vitals)
        return
    }

    internal fun tickEffects(conditions: ChunkConditions, elapsed: Long) {
        tickSpeedReduction(conditions, elapsed)
    }

    private fun tickSpeedReduction(conditions: ChunkConditions, elapsed: Long) {
        val vitals = vitals()
        val penalty =
            speedPenalty(vitals.getBodyTemperature())
        val attribute = player.getAttribute(MOVEMENT_SPEED) ?: return
        if (penalty == 0.0) {
            attribute.removeModifier(
                BODY_TEMPERATURE_SPEED_REDUCTION
            )
        } else {
            attribute.addOrUpdateTransientModifier(
                AttributeModifier(
                    BODY_TEMPERATURE_SPEED_REDUCTION,
                    -penalty,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                )
            )
        }
    }

    public companion object {
        public fun default(player: ServerPlayer): BrokenPromisesPlayer {
            return BrokenPromisesPlayer(
                player,
                MovementStatistics.DEFAULT
            )
        }
    }
}