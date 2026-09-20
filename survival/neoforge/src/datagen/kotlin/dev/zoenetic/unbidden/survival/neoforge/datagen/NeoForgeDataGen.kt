package dev.zoenetic.unbidden.survival.neoforge.datagen

import dev.zoenetic.unbidden.survival.Survival
import dev.zoenetic.unbidden.survival.datagen.SurvivalDataGen
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.data.event.GatherDataEvent

@Mod(Survival.MOD_ID)
class NeoForgeDataGen(modBus: IEventBus) {

    init {
        modBus.addListener(GatherDataEvent.Client::class.java) { event ->
            for (factory in SurvivalDataGen.providers) {
                event.createProvider { output, registries -> factory(output, registries) }
            }
        }
    }
}
