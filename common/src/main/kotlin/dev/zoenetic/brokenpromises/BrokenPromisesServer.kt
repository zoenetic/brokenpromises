package dev.zoenetic.brokenpromises

import dev.zoenetic.brokenpromises.environment.BrokenPromisesChunks
import dev.zoenetic.brokenpromises.heat.HeatSources
import net.minecraft.server.MinecraftServer

public class BrokenPromisesServers(
    public val map: HashMap<MinecraftServer, BrokenPromisesServer> = HashMap()
) {
    public fun tick() {
        map.values.forEach { server ->
            server.tick()
        }
    }

    public companion object {
        public fun new(): BrokenPromisesServers {
            return BrokenPromisesServers(
                map = hashMapOf()
            )
        }
    }
}

public class BrokenPromisesServer(
    public val chunks: BrokenPromisesChunks,
    public val heatSources: HeatSources,
) {
    public fun tick() {
        heatSources.tick()
    }

    public companion object {
        public fun new(): BrokenPromisesServer {
            return BrokenPromisesServer(
                BrokenPromisesChunks.new(),
                HeatSources.new()
            )
        }
    }
}