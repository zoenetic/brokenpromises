package dev.zoenetic.brokenpromises.survival.datagen

import dev.zoenetic.brokenpromises.survival.registry.BrokenPromisesItems
import net.minecraft.core.HolderLookup
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import java.util.concurrent.CompletableFuture

class BrokenPromisesRecipes(registries: HolderLookup.Provider, output: RecipeOutput) :
    RecipeProvider(registries, output) {

    override fun buildRecipes() {
        shapeless(RecipeCategory.MISC, Items.STICK)
            .requires(BrokenPromisesItems.FIREWOOD_ITEM)
            .unlockedBy(
                "has_firewood",
                has(BrokenPromisesItems.FIREWOOD_ITEM)
            ).save(output)

        shaped(RecipeCategory.MISC, Items.CAMPFIRE, 1)
            .pattern("ff")
            .pattern("ff")
            .define('f', BrokenPromisesItems.FIREWOOD_ITEM)
            .unlockedBy(
                "has_firewood",
                has(BrokenPromisesItems.FIREWOOD_ITEM)

            )
            .save(output)

        shaped(RecipeCategory.MISC, BrokenPromisesItems.FUELLED_TORCH_ITEM)
            .pattern("c")
            .pattern("s")
            .define('c', ItemTags.COALS)
            .define('s', Items.STICK)
            .unlockedBy(
                "has_stone_pickaxe", has(Items.STONE_PICKAXE)
            )
            .save(output)

    }

    private class Runner(
        packOutput: PackOutput,
        registries: CompletableFuture<HolderLookup.Provider>,
    ) : RecipeProvider.Runner(packOutput, registries) {

        override fun createRecipeProvider(
            registries: HolderLookup.Provider,
            output: RecipeOutput,
        ): RecipeProvider = BrokenPromisesRecipes(registries, output)

        override fun getName(): String = "Broken Promises: Survival recipes"
    }

    companion object {
        fun runner(
            output: PackOutput,
            registries: CompletableFuture<HolderLookup.Provider>,
        ): DataProvider = Runner(output, registries)
    }
}