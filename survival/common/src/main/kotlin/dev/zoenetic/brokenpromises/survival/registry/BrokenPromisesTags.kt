package dev.zoenetic.brokenpromises.survival.registry

import dev.zoenetic.brokenpromises.survival.Survival
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

public object BrokenPromisesTags {

    public val EMITTERS: TagKey<Block> = TagKey.create(
        Registries.BLOCK,
        Identifier.fromNamespaceAndPath(
            Survival.NAMESPACE, "emitters"
        )
    )

    public val FUELLED_LIGHT_SOURCE_BLOCKS: TagKey<Block> = TagKey.create(
        Registries.BLOCK,
        Identifier.fromNamespaceAndPath(
            Survival.NAMESPACE, "fuelled_light_source_blocks"
        )
    )

}