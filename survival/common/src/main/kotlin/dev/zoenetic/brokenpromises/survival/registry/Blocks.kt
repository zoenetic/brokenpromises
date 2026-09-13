package dev.zoenetic.brokenpromises.survival.registry

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.block.FirewoodBlock
import net.minecraft.core.Holder
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour

public object Blocks {
    public val FIREWOOD: Holder<Block> = Survival.platform.registrar.block(
        "firewood",
        BlockBehaviour.Properties.of().strength(2F),
        ::FirewoodBlock
    )

    public fun init() {}
}