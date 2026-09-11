package dev.zoenetic.brokenpromises.survival.fabric

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.client.HEARTBEAT
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

public object SurvivalClientFabric : ClientModInitializer {

    override fun onInitializeClient() {
        Survival.LOGGER.info("Broken Promises: Survival client starting on Fabric")

        ClientTickEvents.END_CLIENT_TICK.register {
            HEARTBEAT.tick()
        }
    }
}
