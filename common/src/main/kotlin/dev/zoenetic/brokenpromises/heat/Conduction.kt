package dev.zoenetic.brokenpromises.heat

import dev.zoenetic.brokenpromises.BrokenPromises.MOD_ID
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

public enum class Conduction

public val METAL: TagKey<Block> = TagKey.create(
    Registries.BLOCK,
    Identifier.fromNamespaceAndPath(MOD_ID, "metal")
)

public enum class OnSurface(public val conductance: Double) {
    SNOW(1.2),
    STONE(1.5),
    METAL(2.0),
    ICE(2.5),
    MAGMA(3.0),
}

public fun ServerPlayer.getOnSurface(): OnSurface? {
    val on = blockStateOn
    if (on.`is`(Blocks.MAGMA_BLOCK)) return OnSurface.MAGMA
    if (on.`is`(BlockTags.SNOW)) return OnSurface.SNOW
    if (on.`is`(BlockTags.BASE_STONE_OVERWORLD)) return OnSurface.STONE
    if (on.`is`(METAL)) return OnSurface.METAL
    if (on.`is`(BlockTags.ICE)) return OnSurface.ICE
    return null
}

public enum class InMedium(public val conductance: Double) {
    RAIN(5.0),
    WATER(25.0),
    POWDER_SNOW(50.0),
    LAVA(1000.0),
}

public fun ServerPlayer.getInMedium(): InMedium? {
    if (this.isInLava) return InMedium.LAVA
    if (this.isInWater) return InMedium.WATER
    if (this.isInPowderSnow) return InMedium.POWDER_SNOW
    if (this.isInWaterOrRain) return InMedium.RAIN
    return null
}