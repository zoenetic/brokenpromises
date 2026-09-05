package dev.zoenetic.brokenpromises.platform

import dev.zoenetic.brokenpromises.vitals.Vitals
import net.minecraft.server.level.ServerPlayer

public interface Platform {
    public val name: String

    public val isDevelopmentEnvironment: Boolean

    public  fun isModLoaded(modId: String): Boolean

    public fun vitals(player: ServerPlayer): Vitals
    public fun setVitals(player: ServerPlayer, value: Vitals)
}
