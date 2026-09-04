package dev.zoenetic.brokenpromises.fabric

import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.commands.exposureCommand
import dev.zoenetic.brokenpromises.commands.rootCommand
import dev.zoenetic.brokenpromises.commands.watchExposureCommand
import dev.zoenetic.brokenpromises.environment.tickEnvironment
import dev.zoenetic.brokenpromises.commands.tickWatchers
import dev.zoenetic.brokenpromises.heat.dropHeatSourceState
import dev.zoenetic.brokenpromises.heat.rebuildHeatSourceState
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

public object BrokenPromisesFabric : ModInitializer {

    override fun onInitialize() {
        BrokenPromises.init(FabricPlatform)
        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment -> dispatcher.register(
            rootCommand.then(exposureCommand).then(watchExposureCommand)
        ) }
        ServerChunkEvents.CHUNK_LOAD.register { level, chunk, generated ->
            chunk.rebuildHeatSourceState()
        }
        ServerChunkEvents.CHUNK_UNLOAD.register { level, chunk ->
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
