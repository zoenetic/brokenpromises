package dev.zoenetic.unbidden.survival.datagen

import dev.zoenetic.unbidden.survival.registry.UnbiddenItems
import net.minecraft.core.HolderLookup
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import java.util.concurrent.CompletableFuture

class UnbiddenRecipes(registries: HolderLookup.Provider, output: RecipeOutput) :
    RecipeProvider(registries, output) {

    override fun buildRecipes() {
        shapeless(RecipeCategory.MISC, Items.STICK)
            .requires(UnbiddenItems.FIREWOOD_ITEM)
            .unlockedBy(
                "has_firewood",
                has(UnbiddenItems.FIREWOOD_ITEM)
            ).save(output)

        shaped(RecipeCategory.MISC, Items.CAMPFIRE, 1)
            .pattern("ff")
            .pattern("ff")
            .define('f', UnbiddenItems.FIREWOOD_ITEM)
            .unlockedBy(
                "has_firewood",
                has(UnbiddenItems.FIREWOOD_ITEM)

            )
            .save(output)

        shaped(RecipeCategory.MISC, UnbiddenItems.FUELLED_TORCH_ITEM)
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
        ): RecipeProvider = UnbiddenRecipes(registries, output)

        override fun getName(): String = "Unbidden: Survival recipes"
    }

    companion object {
        fun runner(
            output: PackOutput,
            registries: CompletableFuture<HolderLookup.Provider>,
        ): DataProvider = Runner(output, registries)
    }
}