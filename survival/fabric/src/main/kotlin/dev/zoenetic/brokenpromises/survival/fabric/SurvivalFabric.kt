package dev.zoenetic.brokenpromises.survival.fabric

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.debug.*
import dev.zoenetic.brokenpromises.survival.registry.Sounds.HEARTBEAT_SOUND_EVENT
import dev.zoenetic.brokenpromises.survival.registry.Sounds.HEARTBEAT_SOUND_ID
import dev.zoenetic.brokenpromises.survival.state.ChunkHeatSources
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.Exertion
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries

public object SurvivalFabric : ModInitializer {

    override fun onInitialize() {
        Survival.init(FabricPlatform)

        Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            HEARTBEAT_SOUND_ID,
            HEARTBEAT_SOUND_EVENT
        )

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
            ChunkHeatSources.rebuild(chunk)
        }

        ServerTickEvents.END_LEVEL_TICK.register { level ->
            PlayerConditions.tick(level)
            Vitals.tick(level)
            Exertion.tick(level)
        }

        ServerTickEvents.END_SERVER_TICK.register { server ->
            WatcherRegistry.tick(server)
        }
    }
}
