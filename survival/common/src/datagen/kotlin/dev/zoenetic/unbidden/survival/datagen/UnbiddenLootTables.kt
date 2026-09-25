package dev.zoenetic.unbidden.survival.datagen

import dev.zoenetic.unbidden.survival.registry.UnbiddenBlocks
import net.minecraft.core.HolderLookup
import net.minecraft.data.DataProvider
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class UnbiddenLootTables : LootTableSubProvider {
    override fun generate(output: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
        output.accept(
            UnbiddenBlocks.TORCH.lootTable.orElseThrow(),
            LootTable.lootTable()
                .withPool(
                    LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1f))
                        .add(LootItem.lootTableItem(UnbiddenBlocks.TORCH))
                        .`when`(ExplosionCondition.survivesExplosion())
                )
        )
    }

    companion object {
        fun factory(output: PackOutput, registries: CompletableFuture<HolderLookup.Provider>): DataProvider = LootTableProvider(
            output,
            emptySet(),
            listOf(
                LootTableProvider.SubProviderEntry(
                    { UnbiddenLootTables() },
                    LootContextParamSets.BLOCK,
                )
            ),
            registries
        )
    }
}