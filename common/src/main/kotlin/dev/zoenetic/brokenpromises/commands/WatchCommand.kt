package dev.zoenetic.brokenpromises.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.zoenetic.brokenpromises.environment.exposureCache
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.UUID
import java.util.function.Supplier
import kotlin.math.roundToInt

private val watchers: MutableSet<UUID> = mutableSetOf()

public val watchExposureCommand: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("watch")
    .executes { context ->
        val source = context.source
        val player = source.playerOrException
        if (!watchers.contains(player.uuid)) {
            watchers.add(player.uuid)
            source.sendSuccess({
                Component.literal("Added watcher")
            }, false)
        } else {
            watchers.remove(player.uuid)
            source.sendSuccess({
                Component.literal("Removed watcher")
            }, false)
        }
        return@executes 1
    }

public fun MinecraftServer.tickWatchers(tick: Long) {
    for (uuid in watchers) {
        val player = this.playerList.getPlayer(uuid) ?: continue
        player.tickWatcher(tick)
    }
}

internal fun ServerPlayer.tickWatcher(tick: Long) {
    val cached = exposureCache[uuid]
    if (cached != null) {
        val elapsed = tick - cached.tick
        if (elapsed == 0L) {
            this.sendSystemMessage(Component.literal("${cached.exposure.temperature.value.roundToInt()} C"), true)
        }
    }
}