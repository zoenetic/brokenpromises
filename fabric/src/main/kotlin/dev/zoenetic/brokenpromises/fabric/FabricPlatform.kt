package dev.zoenetic.brokenpromises.fabric

import dev.zoenetic.brokenpromises.platform.Platform
import net.fabricmc.loader.api.FabricLoader

public object FabricPlatform : Platform {
    override val name: String = "Fabric"

    override val isDevelopmentEnvironment: Boolean
        get() = FabricLoader.getInstance().isDevelopmentEnvironment

    override fun isModLoaded(modId: String): Boolean =
        FabricLoader.getInstance().isModLoaded(modId)
}
