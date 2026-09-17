package dev.zoenetic.brokenpromises.survival.interaction

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.block.SurvivalBlockProperties.FUEL_LEVEL
import dev.zoenetic.brokenpromises.survival.block.SurvivalBlockProperties.MAX_FUEL
import dev.zoenetic.brokenpromises.survival.fire.ClientFireAttempt
import dev.zoenetic.brokenpromises.survival.fire.FireStarting
import dev.zoenetic.brokenpromises.survival.fuel.FuelValue
import dev.zoenetic.brokenpromises.survival.registry.BrokenPromisesSounds
import dev.zoenetic.brokenpromises.survival.state.isLit
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.CampfireBlock.LIT
import net.minecraft.world.level.block.state.BlockState

public object CampfireInteractions {

    @JvmStatic
    public fun maybeLight(
        state: BlockState, level: Level, pos: BlockPos,
        player: Player
    ): InteractionResult {
        if (state.isLit()) return InteractionResult.PASS
        if (!player.hasEmptyHands()) return InteractionResult.PASS

        val time = level.gameTime
        if (level.isClientSide) {
            ClientFireAttempt.record(time)
            return InteractionResult.SUCCESS
        }

        val fireAttempts = Survival.serverState.fireAttempts()
        val attempt = fireAttempts.recordAttempt(player.uuid, pos, time)
        val lit = level.random.nextDouble() < FireStarting.chance(attempt)

        if (lit) {
            level.setBlock(pos, state.setValue(LIT, true), 3)
            fireAttempts.clear(player.uuid, pos)
        }

        level.playSound(null, pos, lightingSound(lit), SoundSource.BLOCKS, 1F, 1F)
        return InteractionResult.SUCCESS
    }

    @JvmStatic
    public fun maybeRefuel(
        state: BlockState,
        itemStack: ItemStack,
        level: Level,
        pos: BlockPos,
        player: Player
    ): Boolean {
        val fuelValueOfItem = FuelValue.of(itemStack.item)
        if (fuelValueOfItem == 0) return false
        val currentFuel = state.getValue(FUEL_LEVEL)
        if (currentFuel == MAX_FUEL) return false
        if (level.isClientSide) return true

        val newFuel = (currentFuel + fuelValueOfItem).coerceAtMost(MAX_FUEL)
        level.setBlock(pos, state.setValue(FUEL_LEVEL, newFuel), 3)
        itemStack.consume(1, player)
        level.playSound(null, pos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 1F, 1F)
        return true
    }

    private fun Player.hasEmptyHands(): Boolean =
        getItemInHand(InteractionHand.MAIN_HAND).isEmpty &&
                getItemInHand(InteractionHand.OFF_HAND).isEmpty

    private fun lightingSound(lit: Boolean): SoundEvent =
        if (lit) BrokenPromisesSounds.FIRE_SUCCESS else BrokenPromisesSounds.FIRE_FAILURE
}
