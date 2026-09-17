package dev.zoenetic.brokenpromises.survival.registry

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.blockentity.FuelledTorchBlockEntity
import dev.zoenetic.brokenpromises.survival.platform.getValue
import dev.zoenetic.brokenpromises.survival.registry.BrokenPromisesBlocks.FUELLED_TORCH_BLOCK
import net.minecraft.world.level.block.entity.BlockEntityType

public object BrokenPromisesBlockEntities {

    public val FUELLED_TORCH_BLOCK_ENTITY: BlockEntityType<*> by
    Survival.platform.register.blockEntity(
        "fuelled_torch_block",
        ::FuelledTorchBlockEntity
    ) { setOf(FUELLED_TORCH_BLOCK) }

}