package dev.zoenetic.brokenpromises.survival.platform

import dev.zoenetic.brokenpromises.survival.conditions.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap

public interface Platform {
    public val name: String
    public val isDevelopmentEnvironment: Boolean
    public fun isModLoaded(modId: String): Boolean

    public val register: Register

    public val emitters: ChunkView<Long2ObjectOpenHashMap<Long>>
    public val playerConditions: PlayerStore<PlayerConditions>
    public val vitals: SyncedPlayerStore<Vitals>
}