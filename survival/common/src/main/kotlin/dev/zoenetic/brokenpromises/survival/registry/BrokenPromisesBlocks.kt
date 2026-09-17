package dev.zoenetic.brokenpromises.survival.registry

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.block.FirewoodBlock
import dev.zoenetic.brokenpromises.survival.block.FuelledTorchBlock
import dev.zoenetic.brokenpromises.survival.block.FuelledWallTorchBlock
import dev.zoenetic.brokenpromises.survival.platform.getValue
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.PushReaction

public object BrokenPromisesBlocks {

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

    public val ALL: List<Block> = listOf(
        FIREWOOD, FUELLED_TORCH_BLOCK, FUELLED_WALL_TORCH_BLOCK
    )

    public fun init() {}
}