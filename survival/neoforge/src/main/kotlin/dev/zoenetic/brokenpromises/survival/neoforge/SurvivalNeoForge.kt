package dev.zoenetic.brokenpromises.survival.neoforge

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.debug.*
import dev.zoenetic.brokenpromises.survival.registry.Sounds.HEARTBEAT_SOUND_EVENT
import dev.zoenetic.brokenpromises.survival.registry.Sounds.HEARTBEAT_SOUND_ID
import dev.zoenetic.brokenpromises.survival.state.ChunkHeatSources
import dev.zoenetic.brokenpromises.survival.vitals.Exertion
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.level.ChunkEvent
import net.neoforged.neoforge.event.tick.LevelTickEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import java.util.function.Supplier

@Mod(Survival.MOD_ID)
public class SurvivalNeoForge(modBus: IEventBus) {
    init {
        Survival.init(NeoForgePlatform)
        NeoForgePlatform.ATTACHMENTS.register(modBus)

        NeoForgePlatform.SOUND_EVENTS.register(
            HEARTBEAT_SOUND_ID.path,
            Supplier { HEARTBEAT_SOUND_EVENT },
        )
        NeoForgePlatform.SOUND_EVENTS.register(modBus)

        val bus = NeoForge.EVENT_BUS
        bus.addListener(RegisterCommandsEvent::class.java) { event ->
            event.dispatcher.register(
                rootCommand.then(
                    survivalCommand
                        .then(setBodyTemperatureCommand)
                        .then(watchCommand)
                )
            )
        }
        bus.addListener(PlayerEvent.PlayerLoggedInEvent::class.java) { event ->
            val player = event.entity as? ServerPlayer ?: return@addListener
            WatcherRegistry.addDev(player)
        }
        bus.addListener(PlayerEvent.PlayerLoggedOutEvent::class.java) { event ->
            val player = event.entity as? ServerPlayer ?: return@addListener
            Exertion.remove(player.uuid)
            WatcherRegistry.remove(player.uuid)
        }
        bus.addListener(ChunkEvent.Load::class.java) { event ->
            val chunk = event.chunk
            ChunkHeatSources.rebuild(chunk)
        }
        bus.addListener(LevelTickEvent.Post::class.java) { event ->
            val level = event.level
            if (level !is ServerLevel) return@addListener
            Survival.tick(level)
        }
        bus.addListener(ServerTickEvent.Post::class.java) { event ->
            WatcherRegistry.tick(event.server)
        }
    }
}
