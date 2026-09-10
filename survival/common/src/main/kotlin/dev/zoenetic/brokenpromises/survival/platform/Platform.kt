package dev.zoenetic.brokenpromises.survival.platform

import dev.zoenetic.brokenpromises.survival.state.HeatSourceIndex
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.Vitals

public interface Platform {
    public val name: String
    public val isDevelopmentEnvironment: Boolean
    public fun isModLoaded(modId: String): Boolean

    public val heatSources: ChunkView<HeatSourceIndex>
    public val playerConditions: PlayerStore<PlayerConditions>
    public val vitals: SyncedPlayerStore<Vitals>
}
