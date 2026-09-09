package dev.zoenetic.brokenpromises.survival.neoforge

import dev.zoenetic.brokenpromises.survival.platform.PlayerStore
import dev.zoenetic.brokenpromises.survival.platform.SyncedPlayerStore
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.attachment.AttachmentType
import java.util.function.Supplier

internal class NeoForgePlayerStore<T : Any>(
    private val type: Supplier<AttachmentType<T>>,
) : PlayerStore<T> {
    override fun get(player: ServerPlayer): T = player.getData(type)
    override fun set(player: ServerPlayer, value: T) {
        player.setData(type, value)
    }
}

internal class NeoForgeSyncedPlayerStore<T : Any>(
    private val type: Supplier<AttachmentType<T>>,
) : SyncedPlayerStore<T> {
    override fun get(player: Player): T = player.getData(type)
    override fun set(player: ServerPlayer, value: T) {
        player.setData(type, value)
    }
}
