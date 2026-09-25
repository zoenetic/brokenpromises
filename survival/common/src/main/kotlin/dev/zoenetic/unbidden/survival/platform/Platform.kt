package dev.zoenetic.unbidden.survival.platform

import dev.zoenetic.unbidden.survival.conditions.PlayerConditions
import dev.zoenetic.unbidden.survival.vitals.Vitals
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap

public interface Platform {
    public val name: String
    public val isDevelopmentEnvironment: Boolean
    public fun isModLoaded(modId: String): Boolean

    public val register: Register
    public val wideners: Widener

    public val emitters: ChunkStore<Long2LongOpenHashMap>
    public val playerConditions: PlayerStore<PlayerConditions>
    public val vitals: SyncedPlayerStore<Vitals>
}