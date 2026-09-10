package dev.zoenetic.brokenpromises.survival.fabric

import dev.zoenetic.brokenpromises.survival.platform.ChunkView
import dev.zoenetic.brokenpromises.survival.platform.PlayerStore
import dev.zoenetic.brokenpromises.survival.platform.SyncedPlayerStore
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.chunk.LevelChunk

internal class FabricChunkView<T : Any>(
    private val type: AttachmentType<T>,
) : ChunkView<T> {
    override fun get(chunk: LevelChunk): T? = chunk.getAttached(type)
}

internal class FabricPlayerStore<T : Any>(
    private val type: AttachmentType<T>,
) : PlayerStore<T> {
    override fun get(player: ServerPlayer): T? = player.getAttached(type)
    override fun set(player: ServerPlayer, value: T) {
        player.setAttached(type, value)
    }
}

internal class FabricSyncedPlayerStore<T : Any>(
    private val type: AttachmentType<T>,
) : SyncedPlayerStore<T> {
    override fun get(player: Player): T? = player.getAttached(type)
    override fun set(player: Player, value: T) {
        player.setAttached(type, value)
    }
}
