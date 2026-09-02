package dev.zoenetic.brokenpromises.fabric

import dev.zoenetic.brokenpromises.platform.Platform
import net.fabricmc.loader.api.FabricLoader

object FabricPlatform : Platform {
    override val name = "Fabric"

    override val isDevelopmentEnvironment
        get() = FabricLoader.getInstance().isDevelopmentEnvironment

    override fun isModLoaded(modId: String) =
        FabricLoader.getInstance().isModLoaded(modId)
}
