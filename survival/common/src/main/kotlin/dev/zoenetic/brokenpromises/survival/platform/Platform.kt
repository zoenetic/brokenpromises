package dev.zoenetic.brokenpromises.survival.platform

import dev.zoenetic.brokenpromises.survival.state.HeatSourceIndex
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.chunk.LevelChunk

public enum class PlatformName {
    FABRIC,
    NEOFORGE,
}

public fun PlatformName.id(): String = this.name.lowercase()

public enum class Store {
    CHUNK_HEAT_SOURCES,
    PLAYER_CONDITIONS,
    PLAYER_VITALS,
}

public fun Store.id(): String = this.name.lowercase()

public interface ChunkView<T> {
    public fun get(chunk: LevelChunk): T
}

public interface ChunkStore<T> {
    public fun get(chunk: LevelChunk): T
    public fun set(chunk: LevelChunk, value: T)
}

public interface PlayerStore<T> {
    public fun get(player: ServerPlayer): T
    public fun set(player: ServerPlayer, value: T)
}

public interface SyncedPlayerStore<T> {
    public fun get(player: Player): T
    public fun set(player: ServerPlayer, value: T)
}

public interface Platform {
    public val name: PlatformName
    public val isDevelopmentEnvironment: Boolean
    public fun isModLoaded(modId: String): Boolean

    public val heatSources: ChunkView<HeatSourceIndex>
    public val playerConditions: PlayerStore<PlayerConditions>
    public val vitals: SyncedPlayerStore<Vitals>
}
