package dev.zoenetic.brokenpromises.survival.platform

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Holder
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import kotlin.reflect.KProperty

public operator fun <T : Any> Holder<T>.getValue(
    thisRef: Any?,
    property: KProperty<*>
): T = this.value()

public interface Register {

    public fun block(
        name: String,
        properties: BlockBehaviour.Properties,
        factory: (BlockBehaviour.Properties) -> Block
    ): Holder<Block>

    public fun blockEntity(
        name: String,
        factory: (BlockEntityType<*>, BlockPos, BlockState) -> BlockEntity,
        blocks: () -> Set<Block>
    ): Holder<BlockEntityType<*>>

    public fun blockItem(
        name: String,
        block: () -> Block,
        properties: Item.Properties = Item.Properties()
    ): Holder<Item>

    public fun sound(
        name: String,
        factory: (Identifier) -> SoundEvent
    ): Holder<SoundEvent>

    public fun standingAndWallBlockItem(
        name: String,
        block: () -> Block,
        wallBlock: () -> Block,
        attachmentDirection: Direction,
        properties: Item.Properties = Item.Properties()
    ): Holder<Item>

}
