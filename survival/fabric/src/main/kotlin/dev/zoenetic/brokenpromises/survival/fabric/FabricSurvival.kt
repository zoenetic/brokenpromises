package dev.zoenetic.brokenpromises.survival.fabric

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.debug.*
import dev.zoenetic.brokenpromises.survival.emission.Emitters.rebuildEmitters
import dev.zoenetic.brokenpromises.survival.vitals.Exertion
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

        ServerChunkEvents.CHUNK_LOAD.register { _, chunk, _ ->
            chunk.rebuildEmitters()
        }

        ServerTickEvents.END_LEVEL_TICK.register { level ->
            Survival.tick(level)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            WatcherRegistry.tick(server)
        }
    }
}
