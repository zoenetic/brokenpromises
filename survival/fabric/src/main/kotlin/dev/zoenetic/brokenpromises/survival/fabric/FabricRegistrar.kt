package dev.zoenetic.brokenpromises.survival.fabric

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.platform.Registrar
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour

public object FabricRegistrar : Registrar {

    override fun block(
        name: String,
        properties: BlockBehaviour.Properties,
        factory: (BlockBehaviour.Properties) -> Block
    ): Holder<Block> {
        val key = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        )
        return Registry.registerForHolder(
            BuiltInRegistries.BLOCK,
            key,
            factory(properties.setId(key))
        )
    }

    override fun blockItem(
        name: String,
        block: Holder<Block>,
        properties: Item.Properties
    ): Holder<Item> {
        val key = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        )
        return Registry.registerForHolder(
            BuiltInRegistries.ITEM,
            key,
            BlockItem(block.value(), properties.setId(key))
        )
    }

    override fun sound(
        name: String,
        factory: (Identifier) -> SoundEvent
    ): Holder<SoundEvent> {
        val id = Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        return Registry.registerForHolder(
            BuiltInRegistries.SOUND_EVENT,
            ResourceKey.create(Registries.SOUND_EVENT, id),
            factory(id)
        )
    }
}
