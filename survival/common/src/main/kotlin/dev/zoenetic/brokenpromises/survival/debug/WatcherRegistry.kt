package dev.zoenetic.brokenpromises.survival.debug

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.units.Duration
import dev.zoenetic.brokenpromises.survival.units.Time
import dev.zoenetic.brokenpromises.survival.vitals.Exertion
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.Permissions
import net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED
import java.util.*

public object WatcherRegistry {

    public val registry: MutableSet<UUID> = mutableSetOf()

    public fun add(uuid: UUID) {
        registry.add(uuid)
    }

    public fun addDev(player: ServerPlayer) {
        if (player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)
            && Survival.platform.isDevelopmentEnvironment
        ) add(player.uuid)
    }

    public fun remove(uuid: UUID) {
        registry.remove(uuid)
    }

    public fun tick(server: MinecraftServer) {
        val players = server.playerList.playersByUUID
        for (uuid in registry) {
            if (players.contains(uuid)) {
                val player = players[uuid] ?: continue
                val level = player.level()
                val time = Time(level.gameTime)
                val conditions = Survival.platform.playerConditions.get(player) ?: continue
                val vitals = Survival.platform.vitals.get(player) ?: continue
                val exertion = Exertion.get(player.uuid)
                val speed = player.getAttributeValue(MOVEMENT_SPEED)
                val elapsed: Duration = time - conditions.time
                if (elapsed.value == 0L) {
                    player.sendSystemMessage(
                        Component.literal(
                            $$"A: $${
                                String.format(
                                    "%.1f", conditions.temperature.value
                                )
                            }°C, B: $${
                                String.format(
                                    "%.1f", vitals.bodyTemperature.value.toDouble()
                                )
                            }°C, H: $${
                                String.format(
                                    "%.1f", vitals.heartRate.bpm.value
                                )
                            }BPM, S: $${
                                String.format(
                                    "%.1f", speed * 1000
                                )
                            }%, Ex: $${
                                String.format(
                                    "%.1f", exertion?.value ?: 1.0
                                )
                            }MET, Sh: $${
                                String.format(
                                    "%.1f", 100 - (conditions.sky.value * 100)
                                )
                            }% $${if (conditions.isUnderOpenSky) "open sky" else ""}, W: $${
                                String.format(
                                    "%.1f", (conditions.wind.speed)
                                )
                            }"
                        ), true
                    )
                }
            }
        }
    }
}