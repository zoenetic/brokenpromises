package dev.zoenetic.brokenpromises.neoforge

import dev.zoenetic.brokenpromises.platform.Platform
import net.neoforged.fml.loading.FMLLoader

object NeoForgePlatform : Platform {
    override val name = "NeoForge"

    override val isDevelopmentEnvironment
        get() = !FMLLoader.isProduction()

    override fun isModLoaded(modId: String) =
        FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null
}
