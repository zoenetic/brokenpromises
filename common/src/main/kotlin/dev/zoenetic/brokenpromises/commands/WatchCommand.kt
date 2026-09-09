package dev.zoenetic.brokenpromises.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.BrokenPromisesPlayers
import dev.zoenetic.brokenpromises.BrokenPromisesServers
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.Permissions
import java.util.*

public class BrokenPromisesWatchers(
    private val cache: MutableSet<UUID>
) {
    public fun add(uuid: UUID) {
        this.cache.add(uuid)
    }

    public fun addIfDev(player: ServerPlayer, tick: Long) {
        if (player.permissions()
                .hasPermission(Permissions.COMMANDS_GAMEMASTER) && BrokenPromises.platform.isDevelopmentEnvironment
        ) add(player.uuid)
    }

    public fun remove(uuid: UUID) {
        this.cache.remove(uuid)
    }

    public fun tick(servers: BrokenPromisesServers, players: BrokenPromisesPlayers) {
        cache.forEach { uuid ->
            if (!cache.contains(uuid)) return
            if (!players.map.contains(uuid)) return
            servers.map.forEach { (minecraftServer, brokenPromisesServer) ->
                minecraftServer.playerList.playersByUUID.forEach { (uuid, player) ->
                    if (!cache.contains(player.uuid)) return
                    val conditions = brokenPromisesServer.chunks.atPlayer(player)
                    val vitals = BrokenPromises.platform.vitals(player)
                    player.sendSystemMessage(
                        Component.literal(
                            $$"A: $${
                                String.format(
                                    "%.1f", conditions.temperature
                                )
                            }°C, B: $${
                                String.format(
                                    "%.1f", vitals.getBodyTemperature()
                                )
                            }°C, S: $${
                                String.format(
                                    "%.1f", player.speed * 1000
                                )
                            }%, Sh: $${
                                String.format(
                                    "%.1f", 100 - (conditions.shelter.sky.openness * 100)
                                )
                            }% $${if (conditions.shelter.underOpenSky) "open sky" else ""}, W: $${
                                String.format(
                                    "%.1f", (conditions.wind)
                                )
                            }"
                        ), true
                    )
                }
            }
        }
    }

    public val watchCommand: LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("watch")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .executes { context ->
                val source = context.source
                val player = source.playerOrException
                if (!cache.contains(player.uuid)) {
                    add(player.uuid)
                    source.sendSuccess({
                        Component.literal("Added watcher")
                    }, false)
                } else {
                    remove(player.uuid)
                    source.sendSuccess({
                        Component.literal("Removed watcher")
                    }, false)
                }
                return@executes 1
            }

    public companion object {
        public fun new(): BrokenPromisesWatchers {
            return BrokenPromisesWatchers(
                mutableSetOf()
            )
        }
    }
}

public class Watcher(
    public val uuid: UUID,
    public val lastUpdated: Long,
)