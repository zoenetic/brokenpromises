package dev.zoenetic.unbidden.survival.registry

import dev.zoenetic.unbidden.survival.Survival
import dev.zoenetic.unbidden.survival.emission.VANILLA_EMITTERS
import dev.zoenetic.unbidden.survival.fuel.FirewoodBlock
import dev.zoenetic.unbidden.survival.platform.getValue
import dev.zoenetic.unbidden.survival.torch.FuelledTorchBlock
import dev.zoenetic.unbidden.survival.torch.FuelledWallTorchBlock
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.PushReaction

public object UnbiddenBlocks {

    public val FIREWOOD: Block by Survival.platform.register.block(
        "firewood",
        BlockBehaviour.Properties.of().strength(2F),
        ::FirewoodBlock
    )

    public val FUELLED_TORCH_BLOCK: Block by Survival.platform.register.block(
        "fuelled_torch",
        BlockBehaviour.Properties.of().noCollision().instabreak().sound(SoundType.WOOD)
            .pushReaction(
                PushReaction.DESTROY
            )
    ) { props ->
        FuelledTorchBlock(ParticleTypes.FLAME, props)
    }

    public val FUELLED_WALL_TORCH_BLOCK: Block by Survival.platform.register.block(
        "fuelled_wall_torch",
        BlockBehaviour.Properties.of().noCollision().instabreak().sound(SoundType.WOOD)
            .pushReaction(
                PushReaction.DESTROY
            )
    ) { props ->
        FuelledWallTorchBlock(ParticleTypes.FLAME, props)
    }

    public val ALL: List<Block> get() = listOf(
        FIREWOOD, FUELLED_TORCH_BLOCK, FUELLED_WALL_TORCH_BLOCK
    )

    public fun init() {}
}