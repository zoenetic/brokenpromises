package dev.zoenetic.brokenpromises.survival.neoforge

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.client.HEARTBEAT
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.common.NeoForge

@Mod(value = Survival.MOD_ID, dist = [Dist.CLIENT])
public class SurvivalClientNeoForge(modBus: IEventBus) {

    init {
        Survival.LOGGER.info("Broken Promises: Survival client starting on NeoForge")

        val bus = NeoForge.EVENT_BUS

        bus.addListener(ClientTickEvent.Post::class.java) { _ ->
            HEARTBEAT.tick()
        }
    }
}
