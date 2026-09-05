package dev.zoenetic.brokenpromises.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.environment.environmentalConditionsCache
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.Permissions
import net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import java.util.*

private val watchers: MutableSet<UUID> = mutableSetOf()

public fun addWatcher(uuid: UUID) {
    watchers.add(uuid)
}

public fun addDevWatcher(player: ServerPlayer) {
    if (player.permissions()
            .hasPermission(Permissions.COMMANDS_GAMEMASTER) && BrokenPromises.platform.isDevelopmentEnvironment
    ) addWatcher(player.uuid)
}

public val watchCommand: LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("watch")
        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
        .executes { context ->
            val source = context.source
            val player = source.playerOrException
            if (!watchers.contains(player.uuid)) {
                addWatcher(player.uuid)
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
        val player =
            this.playerList.getPlayer(uuid) ?: continue
        player.tickWatcher(tick)
    }
}

internal fun ServerPlayer.tickWatcher(tick: Long) {
    val cached = environmentalConditionsCache[uuid]
    if (cached != null) {
        val elapsed = tick - cached.tick
        if (elapsed == 0L) {
            val airTemperature =
                cached.conditions.temperature.value
            val bodyTemperature =
                BrokenPromises.platform.vitals(this).temperature.value
            val speed = getAttributeValue(MOVEMENT_SPEED)
            this.sendSystemMessage(
                Component.literal(
                    "Ambient: ${
                        String.format(
                            "%.1f", airTemperature
                        )
                    }°C, Body: ${
                        String.format(
                            "%.1f", bodyTemperature
                        )
                    }°C, Speed: ${
                        String.format(
                            "%.1f", speed * 1000
                        )
                    }%"
                ), true
            )
        }
    }
}