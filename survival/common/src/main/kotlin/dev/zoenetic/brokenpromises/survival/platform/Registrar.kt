package dev.zoenetic.brokenpromises.survival.platform

import net.minecraft.core.Holder
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour

public interface Registrar {

    public fun block(
        name: String,
        properties: BlockBehaviour.Properties,
        factory: (BlockBehaviour.Properties) -> Block
    ): Holder<Block>

    public fun blockItem(
        name: String,
        block: Holder<Block>,
        properties: Item.Properties = Item.Properties()
    ): Holder<Item>

    public fun sound(
        name: String,
        factory: (Identifier) -> SoundEvent
    ): Holder<SoundEvent>

}