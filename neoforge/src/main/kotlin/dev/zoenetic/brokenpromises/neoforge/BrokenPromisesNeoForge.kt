package dev.zoenetic.brokenpromises.neoforge

import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.heat.dropHeatSourceState
import dev.zoenetic.brokenpromises.heat.rebuildHeatSourceState
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.level.ChunkEvent

@Mod(BrokenPromises.MOD_ID)
public class BrokenPromisesNeoForge(modBus: IEventBus) {
    init {
        BrokenPromises.init(NeoForgePlatform)

        NeoForge.EVENT_BUS.addListener(ChunkEvent.Load::class.java) { event ->
            val chunk = event.chunk
            if (chunk.level.isClientSide) return@addListener
            chunk.rebuildHeatSourceState()
        }
        NeoForge.EVENT_BUS.addListener(ChunkEvent.Unload::class.java) { event ->
            val chunk = event.chunk
            if (chunk.level.isClientSide) return@addListener
            chunk.dropHeatSourceState()
        }
    }
}
