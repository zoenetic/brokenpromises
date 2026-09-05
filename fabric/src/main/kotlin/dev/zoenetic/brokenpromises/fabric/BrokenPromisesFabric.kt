package dev.zoenetic.brokenpromises.fabric

import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.commands.addDevWatcher
import dev.zoenetic.brokenpromises.commands.exposureCommand
import dev.zoenetic.brokenpromises.commands.rootCommand
import dev.zoenetic.brokenpromises.commands.setBodyTemperatureCommand
import dev.zoenetic.brokenpromises.commands.watchCommand
import dev.zoenetic.brokenpromises.environment.tickEnvironment
import dev.zoenetic.brokenpromises.commands.tickWatchers
import dev.zoenetic.brokenpromises.heat.dropHeatSourceState
import dev.zoenetic.brokenpromises.heat.rebuildHeatSourceState
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

public object BrokenPromisesFabric : ModInitializer {

    override fun onInitialize() {
        BrokenPromises.init(FabricPlatform)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> dispatcher.register(
            rootCommand
                .then(exposureCommand)
                .then(setBodyTemperatureCommand)
                .then(watchCommand)
        ) }
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            if (FabricPlatform.isDevelopmentEnvironment) addDevWatcher(handler.player)
        }
        ServerChunkEvents.CHUNK_LOAD.register { _, chunk, _ ->
            chunk.rebuildHeatSourceState()
        }
        ServerChunkEvents.CHUNK_UNLOAD.register { _, chunk ->
            chunk.dropHeatSourceState()
        }
        ServerTickEvents.END_LEVEL_TICK.register { level ->
            level.tickEnvironment(level.gameTime)
        }
        ServerTickEvents.END_SERVER_TICK.register { server ->
            server.tickWatchers(server.overworld().gameTime)
        }
    }
}
