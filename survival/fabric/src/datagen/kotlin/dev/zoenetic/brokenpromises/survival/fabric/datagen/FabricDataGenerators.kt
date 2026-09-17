package dev.zoenetic.brokenpromises.survival.fabric.datagen

import dev.zoenetic.brokenpromises.survival.datagen.SurvivalDataGen
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object FabricDataGenerators : DataGeneratorEntrypoint {

    override fun onInitializeDataGenerator(generator: FabricDataGenerator) {
        val pack = generator.createPack()
        for (factory in SurvivalDataGen.providers) {
            pack.addProvider { output, registries -> factory(output, registries) }
        }
    }
}
