package dev.zoenetic.brokenpromises.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.environment.environmentalConditionsCache
import dev.zoenetic.brokenpromises.environment.getShelter
import dev.zoenetic.brokenpromises.heat.globalHeatSourceState
import dev.zoenetic.brokenpromises.heat.isHeatSourceBlock
import dev.zoenetic.brokenpromises.heat.isLit
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.Permissions
import net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import java.util.*
import kotlin.math.sqrt

private val watchers: MutableSet<UUID> = mutableSetOf()

public fun addWatcher(uuid: UUID) {
    watchers.add(uuid)
}

public fun addDevWatcher(player: ServerPlayer) {
    if (player.permissions()
            .hasPermission(Permissions.COMMANDS_GAMEMASTER) && BrokenPromises.platform.isDevelopmentEnvironment
    ) addWatcher(player.uuid)
}

public fun removeWatcher(uuid: UUID) {
    watchers.remove(uuid)
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
            val shelter = getShelter()
            val wind = cached.conditions.wind.speed
            val levelState = globalHeatSourceState[level()]
            val chunkState = levelState?.get(chunkPosition().pack())
            val here = blockPosition()
            val nearest = globalHeatSourceState[level()]
                ?.get(chunkPosition().pack())
                ?.long2ObjectEntrySet()
                ?.minByOrNull { BlockPos.of(it.longKey).distSqr(here) }
            val probe = if (nearest == null) "no entries" else {
                val p = BlockPos.of(nearest.longKey)
                val s = level().getBlockState(p)
                "nearest=${s.block.name.string}@${p.toShortString()} d=${
                    "%.1f".format(
                        sqrt(
                            p.distSqr(
                                here
                            )
                        )
                    )
                } " +
                        "src=${s.isHeatSourceBlock()} lit=${s.isLit()} loaded=${
                            level().chunkSource.getChunkNow(
                                chunkPosition().x,
                                chunkPosition().z
                            ) != null
                        }"
            }
            this.sendSystemMessage(
                Component.literal(
                    $$"A: $${
                        String.format(
                            "%.1f", airTemperature
                        )
                    }°C, B: $${
                        String.format(
                            "%.1f", bodyTemperature
                        )
                    }°C, S: $${
                        String.format(
                            "%.1f", speed * 1000
                        )
                    }%, Sh: $${
                        String.format(
                            "%.1f", 100 - (shelter.sky.openness * 100)
                        )
                    }% $${if (shelter.underOpenSky) "open sky" else ""}, W: $${
                        String.format(
                            "%.1f", (wind)
                        )
                    }"
                ), true
            )
        }
    }
}
