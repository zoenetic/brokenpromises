package dev.zoenetic.brokenpromises.survival.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.effects.player.shiverIntensity
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import kotlin.math.sin

public const val SHIVER_AMPLITUDE_DEGREES: Double = 1.5
public const val SHIVER_FREQUENCY: Double = 2.0

private const val SHIVER_YAW_RATIO = 1.37
private const val SHIVER_YAW_SCALE = 0.25

/**
 * Carries a player's shiver intensity across the render-state boundary.
 *
 * `LivingEntityRenderer.setupRotations` is handed a render state and nothing
 * else — a render state deliberately holds no reference back to its entity — so
 * the intensity has to be stashed on the state during `extractRenderState`, the
 * one point where both the entity and its state are in scope.
 *
 * Implemented by `LivingEntityRenderStateMixin`.
 */
public interface ShiverState {
    public fun `brokenpromises$getShiver`(): Double
    public fun `brokenpromises$setShiver`(shiver: Double)
}

/** Client-only. Applies body temperature to what the renderer draws. */
public object ShiverRenderer {

    /** Derive this frame's shiver for [entity] and stash it on [state]. */
    public fun capture(entity: LivingEntity, state: LivingEntityRenderState) {
        val intensity = if (entity is Player) intensityOf(entity) else 0.0
        (state as ShiverState).`brokenpromises$setShiver`(intensity)
    }

    /** Wobble the body's yaw by the intensity [capture] left on [state]. */
    public fun applyToBodyRotation(bodyRot: Float, state: LivingEntityRenderState): Float {
        val intensity = (state as ShiverState).`brokenpromises$getShiver`()
        if (intensity <= 0.0) return bodyRot
        val wobble =
            intensity * SHIVER_AMPLITUDE_DEGREES * sin(state.ageInTicks * SHIVER_FREQUENCY)
        return bodyRot + wobble.toFloat()
    }

    /** Wobble the local player's own hands, at frame rather than tick rate. */
    public fun applyToHeldItems(player: LocalPlayer, frameInterp: Float, poseStack: PoseStack) {
        val intensity = intensityOf(player)
        if (intensity <= 0.0) return
        val phase = (player.tickCount + frameInterp) * SHIVER_FREQUENCY
        val roll = sin(phase) * SHIVER_AMPLITUDE_DEGREES * intensity
        val yaw =
            sin(phase * SHIVER_YAW_RATIO) * SHIVER_AMPLITUDE_DEGREES * SHIVER_YAW_SCALE * intensity
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll.toFloat()))
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw.toFloat()))
    }

    private fun intensityOf(player: Player): Double =
        shiverIntensity(Survival.platform.vitals.get(player).bodyTemperature.value)
}
