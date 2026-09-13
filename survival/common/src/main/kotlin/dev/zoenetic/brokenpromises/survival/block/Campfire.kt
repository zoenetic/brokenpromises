package dev.zoenetic.brokenpromises.survival.block

import dev.zoenetic.brokenpromises.survival.registry.Sounds
import dev.zoenetic.brokenpromises.survival.state.isLit
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.CampfireBlock.LIT
import net.minecraft.world.level.block.state.BlockState
import java.util.*
import kotlin.math.pow

public const val LIGHTING_CAMPFIRE_SLACK: Long = 10L

public val attempts: HashMap<UUID, Pair<Int, Long>> = HashMap()
public var playerAttemptingAt: Long? = null

public fun maybeLightCampfire(
    state: BlockState, level: Level, pos: BlockPos,
    player: Player
): InteractionResult {
    if (state.isLit()) return InteractionResult.PASS
    if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty && player.getItemInHand(
            InteractionHand.OFF_HAND
        ).isEmpty
    ) {
        val time = level.gameTime
        if (level.isClientSide) {
            playerAttemptingAt = time
        } else {
            val uuid = player.uuid
            val playerAttempts = attempts.getOrPut(uuid) { Pair(1, time) }
            val n =
                if (level.gameTime - playerAttempts.second > LIGHTING_CAMPFIRE_SLACK) 1 else playerAttempts.first
            val k = 4.0
            val l = 90.0
            val p = (k / l) * (n / l).pow(k - 1)
            if (level.random.nextFloat() < p) {
                val newState = state.setValue(LIT, true)
                level.setBlock(pos, newState, 3)
                attempts.remove(player.uuid)
                level.playSound(null, pos, Sounds.FIRE_SUCCESS.value(), SoundSource.BLOCKS, 1F, 1F)
                return InteractionResult.SUCCESS
            }
            attempts[player.uuid] = Pair(n + 1, time)
            level.playSound(null, pos, Sounds.FIRE_FAILURE.value(), SoundSource.BLOCKS, 1F, 1F)
            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }
    return InteractionResult.PASS
}