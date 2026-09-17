package dev.zoenetic.brokenpromises.survival.fabric

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.platform.Register
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.StandingAndWallBlockItem
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState

public object FabricRegister : Register {

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

    override fun blockEntity(
        name: String,
        factory: (BlockEntityType<*>, BlockPos, BlockState) -> BlockEntity,
        blocks: () -> Set<Block>
    ): Holder<BlockEntityType<*>> {
        val key = ResourceKey.create(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        )
        lateinit var type: BlockEntityType<*>
        type = BlockEntityType({ pos, state -> factory(type, pos, state) }, blocks())
        return Registry.registerForHolder(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type)
    }

    override fun blockItem(
        name: String,
        block: () -> Block,
        properties: Item.Properties
    ): Holder<Item> {
        val key = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        )
        val holder = Registry.registerForHolder(
            BuiltInRegistries.ITEM,
            key,
            BlockItem(block(), properties.setId(key))
        )
        return holder.value().builtInRegistryHolder()
    }

    override fun sound(
        name: String,
        factory: (Identifier) -> SoundEvent
    ): Holder<SoundEvent> {
        val id = Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        val holder = Registry.registerForHolder(
            BuiltInRegistries.SOUND_EVENT,
            ResourceKey.create(Registries.SOUND_EVENT, id),
            factory(id)
        )
        return holder
    }

    override fun standingAndWallBlockItem(
        name: String,
        block: () -> Block,
        wallBlock: () -> Block,
        attachmentDirection: Direction,
        properties: Item.Properties
    ): Holder<Item> {
        val key = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(Survival.NAMESPACE, name)
        )
        val holder = Registry.registerForHolder(
            BuiltInRegistries.ITEM,
            key,
            StandingAndWallBlockItem(
                block(),
                wallBlock(),
                attachmentDirection,
                properties.setId(key)
            )
        )
        return holder.value().builtInRegistryHolder()
    }
}
