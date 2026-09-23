package dev.zoenetic.unbidden.survival.fabric

import dev.zoenetic.unbidden.survival.Survival
import dev.zoenetic.unbidden.survival.debug.*
import dev.zoenetic.unbidden.survival.emission.EmitterIndex.reconcileEmitters
import dev.zoenetic.unbidden.survival.vitals.Exertion
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

public object FabricSurvival : ModInitializer {

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
            WatcherRegistry.addDev(handler.player)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            Exertion.remove(handler.player.uuid)
            WatcherRegistry.remove(handler.player.uuid)
        }

        ServerChunkEvents.CHUNK_LOAD.register { level, chunk, _ ->
            Survival.serverState.dropSchedule(level).reset(chunk.pos, chunk.reconcileEmitters())
        }

        ServerChunkEvents.CHUNK_UNLOAD.register { level, chunk ->
            Survival.serverState.dropSchedule(level).reset(chunk.pos, null)
        }

        ServerTickEvents.END_LEVEL_TICK.register { level ->
            Survival.tick(level)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            WatcherRegistry.tick(server)
        }
    }
}
