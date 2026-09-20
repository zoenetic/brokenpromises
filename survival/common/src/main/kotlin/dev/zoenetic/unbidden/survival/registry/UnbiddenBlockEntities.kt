package dev.zoenetic.unbidden.survival.registry

import dev.zoenetic.unbidden.survival.Survival
import dev.zoenetic.unbidden.survival.platform.getValue
import dev.zoenetic.unbidden.survival.registry.UnbiddenBlocks.FUELLED_TORCH_BLOCK
import dev.zoenetic.unbidden.survival.torch.FuelledTorchBlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType

public object UnbiddenBlockEntities {

    public val FUELLED_TORCH_BLOCK_ENTITY: BlockEntityType<*> by
    Survival.platform.register.blockEntity(
        "fuelled_torch_block",
        ::FuelledTorchBlockEntity
    ) { setOf(FUELLED_TORCH_BLOCK) }

}