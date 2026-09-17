package dev.zoenetic.brokenpromises.survival.datagen

import dev.zoenetic.brokenpromises.survival.registry.BrokenPromisesBlocks
import net.minecraft.data.loot.LootTableSubProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import java.util.function.BiConsumer

class BrokenPromisesLootTables : LootTableSubProvider {
    override fun generate(output: BiConsumer<ResourceKey<LootTable>, LootTable.Builder>) {
        output.accept(
            BrokenPromisesBlocks.FUELLED_TORCH_BLOCK.lootTable.orElseThrow(),
            LootTable.lootTable()
                .withPool(
                    LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1f))
                        .add(LootItem.lootTableItem(BrokenPromisesBlocks.FUELLED_TORCH_BLOCK))
                        .`when`(ExplosionCondition.survivesExplosion())
                )
        )
        output.accept(
            BrokenPromisesBlocks.FUELLED_WALL_TORCH_BLOCK.lootTable.orElseThrow(),
            LootTable.lootTable()
                .withPool(
                    LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1f))
                        .add(LootItem.lootTableItem(BrokenPromisesBlocks.FUELLED_TORCH_BLOCK))
                        .`when`(ExplosionCondition.survivesExplosion())
                )
        )
    }
}