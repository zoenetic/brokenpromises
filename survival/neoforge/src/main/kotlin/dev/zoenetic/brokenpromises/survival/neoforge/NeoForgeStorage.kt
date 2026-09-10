package dev.zoenetic.brokenpromises.survival.neoforge

import dev.zoenetic.brokenpromises.survival.platform.ChunkView
import dev.zoenetic.brokenpromises.survival.platform.PlayerStore
import dev.zoenetic.brokenpromises.survival.platform.SyncedPlayerStore
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.chunk.LevelChunk
import net.neoforged.neoforge.attachment.AttachmentType
import java.util.function.Supplier

internal class NeoForgeChunkView<T : Any>(
    val type: Supplier<AttachmentType<T>>,
) : ChunkView<T> {
    override fun get(chunk: LevelChunk): T = chunk.getData(type)
}

internal class NeoForgePlayerStore<T : Any>(
    val type: Supplier<AttachmentType<T>>,
) : PlayerStore<T> {
    override fun get(player: ServerPlayer): T? = player.getExistingDataOrNull(type)
    override fun set(player: ServerPlayer, value: T) {
        player.setData(type, value)
    }
}

internal class NeoForgeSyncedPlayerStore<T : Any>(
    val type: Supplier<AttachmentType<T>>,
) : SyncedPlayerStore<T> {
    override fun get(player: Player): T? = player.getExistingDataOrNull(type)
    override fun set(player: Player, value: T) {
        player.setData(type, value)
    }
}
