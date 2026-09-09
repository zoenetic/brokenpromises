package dev.zoenetic.brokenpromises.survival.fabric

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.commands.rootCommand
import dev.zoenetic.brokenpromises.survival.commands.setBodyTemperatureCommand
import dev.zoenetic.brokenpromises.survival.commands.survivalCommand
import dev.zoenetic.brokenpromises.survival.debug.watchCommand
import dev.zoenetic.brokenpromises.survival.debug.watchers
import dev.zoenetic.brokenpromises.survival.state.ChunkHeatSources
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

public object SurvivalFabric : ModInitializer {

    override fun onInitialize() {
        Survival.init(FabricPlatform)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                rootCommand.then(
                    survivalCommand
                        .then(setBodyTemperatureCommand)
                        .then(watchCommand)
                )
            )
        }
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            watchers.add(handler.player.uuid)
        }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            watchers.remove(handler.player.uuid)
        }
        ServerChunkEvents.CHUNK_LOAD.register { _, chunk, _ ->
            ChunkHeatSources.rebuild(chunk)
        }
        ServerTickEvents.END_LEVEL_TICK.register { level ->
            PlayerConditions.tick(level)
            Vitals.tick(level)
        }
        ServerTickEvents.END_SERVER_TICK.register { server ->
            watchers.tick(server)
        }
    }
}
