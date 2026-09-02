package dev.zoenetic.brokenpromises.neoforge

import dev.zoenetic.brokenpromises.BrokenPromises
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod

/**
 * KotlinLangForge (`modLoader = "klf"` in neoforge.mods.toml) accepts either an
 * `object` or a class with a public constructor. A class taking the mod event
 * bus is the more useful shape.
 */
@Mod(BrokenPromises.MOD_ID)
class BrokenPromisesNeoForge(modBus: IEventBus) {
    init {
        BrokenPromises.init(NeoForgePlatform)
    }
}
