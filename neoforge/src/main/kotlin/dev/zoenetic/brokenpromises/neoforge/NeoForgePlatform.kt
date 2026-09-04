package dev.zoenetic.brokenpromises.neoforge

import dev.zoenetic.brokenpromises.platform.Platform
import net.neoforged.fml.loading.FMLLoader

public object NeoForgePlatform : Platform {
    override val name: String = "NeoForge"

    override val isDevelopmentEnvironment: Boolean
        get() = !FMLLoader.getCurrent().isProduction

    override fun isModLoaded(modId: String): Boolean =
        FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null
}
