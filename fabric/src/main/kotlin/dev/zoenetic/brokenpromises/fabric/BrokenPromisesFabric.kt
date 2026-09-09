package dev.zoenetic.brokenpromises.fabric

import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.commands.*
import dev.zoenetic.brokenpromises.environment.dropConditionsCache
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

public object BrokenPromisesFabric : ModInitializer {

    override fun onInitialize() {
        val bp = BrokenPromises.init(FabricPlatform)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                rootCommand
                    .then(setBodyTemperatureCommand)
                    .then(watchCommand)
            )
        }
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            addDevWatcher(handler.player)
        }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            removeWatcher(handler.player.uuid)
            dropConditionsCache(handler.player.uuid)
        }
        ServerChunkEvents.CHUNK_LOAD.register { _, chunk, _ ->
            bp.chunks.heatSources.rebuildChunk(chunk)
        }
        ServerChunkEvents.CHUNK_UNLOAD.register { _, chunk ->
            bp.chunks.heatSources.dropChunk(chunk)
        }
        ServerTickEvents.END_LEVEL_TICK.register { level ->
            bp.players.tick()
        }
        ServerTickEvents.END_SERVER_TICK.register { _ ->
            bp.servers.tick()
        }
    }
}
