package dev.zoenetic.unbidden.survival.datagen

import net.minecraft.core.HolderLookup
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import java.util.concurrent.CompletableFuture

typealias ProviderFactory =
            (PackOutput, CompletableFuture<HolderLookup.Provider>) -> DataProvider

object SurvivalDataGen {
    val providers: List<ProviderFactory> = listOf(
        UnbiddenRecipes::factory,
        UnbiddenLootTables::factory,
    )
}
