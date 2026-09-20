package dev.zoenetic.unbidden.survival.fabric

import dev.zoenetic.unbidden.survival.Survival
import dev.zoenetic.unbidden.survival.vitals.client.HEARTBEAT
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

public object FabricClient : ClientModInitializer {

    override fun onInitializeClient() {
        Survival.LOGGER.info("Unbidden: Survival client starting on Fabric")

        ClientTickEvents.END_CLIENT_TICK.register {
            HEARTBEAT.tick()
        }
    }
}
