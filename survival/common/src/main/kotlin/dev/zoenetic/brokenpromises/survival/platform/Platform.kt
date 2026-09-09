package dev.zoenetic.brokenpromises.survival.platform

import dev.zoenetic.brokenpromises.survival.state.HeatSourceIndex
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.chunk.LevelChunk

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

/**
 * A player store whose value the server pushes to clients.
 *
 * Reads take a plain [Player] so render code can reach them; on the client the
 * value is the last copy the server sent. Writes stay on [ServerPlayer]: the
 * server owns the value, the client only observes it.
 */
public interface SyncedPlayerStore<T> {
    public fun get(player: Player): T
    public fun set(player: ServerPlayer, value: T)
}

public interface Platform {
    public val name: String
    public val isDevelopmentEnvironment: Boolean
    public fun isModLoaded(modId: String): Boolean

    public val heatSources: ChunkView<HeatSourceIndex>
    public val playerConditions: PlayerStore<PlayerConditions>
    public val vitals: SyncedPlayerStore<Vitals>
}
