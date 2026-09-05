package dev.zoenetic.brokenpromises.neoforge

import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.commands.*
import dev.zoenetic.brokenpromises.environment.tickEnvironment
import dev.zoenetic.brokenpromises.heat.dropHeatSourceState
import dev.zoenetic.brokenpromises.heat.rebuildHeatSourceState
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

@Mod(BrokenPromises.MOD_ID)
public class BrokenPromisesNeoForge(modBus: IEventBus) {
    init {
        BrokenPromises.init(NeoForgePlatform)
        NeoForgePlatform.ATTACHMENTS.register(modBus)

        val bus = NeoForge.EVENT_BUS
        bus.addListener(RegisterCommandsEvent::class.java) { event ->
            event.dispatcher.register(
                rootCommand
                    .then(setBodyTemperatureCommand)
                    .then(watchCommand)
            )
        }
        bus.addListener(PlayerEvent.PlayerLoggedInEvent::class.java) { event ->
            (event.entity as? ServerPlayer)?.let(::addDevWatcher)
        }
        bus.addListener(ChunkEvent.Load::class.java) { event ->
            val chunk = event.chunk
            if (chunk.level.isClientSide) return@addListener
            chunk.rebuildHeatSourceState()
        }
        bus.addListener(ChunkEvent.Unload::class.java) { event ->
            val chunk = event.chunk
            if (chunk.level.isClientSide) return@addListener
            chunk.dropHeatSourceState()
        }
        bus.addListener(LevelTickEvent.Post::class.java) { event ->
            val level = event.level
            if (level is ServerLevel) level.tickEnvironment(
                level.gameTime
            )
        }
        bus.addListener(ServerTickEvent.Post::class.java) { event ->
            event.server.tickWatchers(event.server.overworld().gameTime)
        }
    }
}
