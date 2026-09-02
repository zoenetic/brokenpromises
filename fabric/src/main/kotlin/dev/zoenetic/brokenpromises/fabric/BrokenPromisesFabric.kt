package dev.zoenetic.brokenpromises.fabric

import dev.zoenetic.brokenpromises.BrokenPromises
import net.fabricmc.api.ModInitializer

/**
 * Referenced from fabric.mod.json with `"adapter": "kotlin"`, which lets the
 * entrypoint be an `object` rather than a class with a no-arg constructor.
 */
object BrokenPromisesFabric : ModInitializer {
    override fun onInitialize() {
        BrokenPromises.init(FabricPlatform)
    }
}
