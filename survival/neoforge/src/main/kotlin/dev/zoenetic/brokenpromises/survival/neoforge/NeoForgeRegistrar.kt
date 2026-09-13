package dev.zoenetic.brokenpromises.survival.neoforge

import dev.zoenetic.brokenpromises.survival.Survival.NAMESPACE
import dev.zoenetic.brokenpromises.survival.platform.Registrar
import net.minecraft.core.Holder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Function
import java.util.function.Supplier

public object NeoForgeRegistrar : Registrar {

    private val attachments: DeferredRegister<AttachmentType<*>> = DeferredRegister.create(
        NeoForgeRegistries.ATTACHMENT_TYPES, NAMESPACE
    )
    private val blocks: DeferredRegister.Blocks = DeferredRegister.createBlocks(NAMESPACE)
    private val items: DeferredRegister.Items = DeferredRegister.createItems(NAMESPACE)
    private val sounds: DeferredRegister<SoundEvent> = DeferredRegister.create(
        BuiltInRegistries.SOUND_EVENT, NAMESPACE
    )

    public fun <T : Any> attachment(
        name: String,
        type: () -> AttachmentType<T>,
    ): DeferredHolder<AttachmentType<*>, AttachmentType<T>> =
        attachments.register(name, Supplier(type))

    override fun block(
        name: String,
        properties: BlockBehaviour.Properties,
        factory: (BlockBehaviour.Properties) -> Block
    ): Holder<Block> =
        blocks.registerBlock(name, factory, Supplier { properties })

    override fun blockItem(
        name: String,
        block: Holder<Block>,
        properties: Item.Properties
    ): Holder<Item> =
        items.registerItem(name, { props ->
            BlockItem(block.value(), props)
        }, Supplier { properties })

    override fun sound(
        name: String,
        factory: (Identifier) -> SoundEvent
    ): Holder<SoundEvent> =
        sounds.register(name, Function { id: Identifier -> factory(id) })

    public fun init(bus: IEventBus) {
        attachments.register(bus)
        blocks.register(bus)
        items.register(bus)
        sounds.register(bus)
    }
}