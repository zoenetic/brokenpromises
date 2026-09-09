package dev.zoenetic.brokenpromises.survival.state

import dev.zoenetic.brokenpromises.survival.effects.player.Breath
import dev.zoenetic.brokenpromises.survival.effects.player.SpeedPenalty
import net.minecraft.server.level.ServerPlayer

public class PlayerEffects(
    public val breath: Breath,
    public val speedPenalty: SpeedPenalty
) {
    public fun tick(player: ServerPlayer) {
        breath.tick(player)
        speedPenalty.tick(player)
    }
}
