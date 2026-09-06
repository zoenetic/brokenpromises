package dev.zoenetic.brokenpromises.platform

import dev.zoenetic.brokenpromises.vitals.Vitals
import net.minecraft.world.entity.player.Player

public interface Platform {
    public val name: String

    public val isDevelopmentEnvironment: Boolean

    public fun isModLoaded(modId: String): Boolean

    public fun vitals(player: Player): Vitals
    public fun setVitals(player: Player, value: Vitals)
}
