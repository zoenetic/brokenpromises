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

public enum class ConductiveSurface(public val conductance: Double) {
    SNOW(1.2),
    STONE(1.5),
    METAL(2.0),
    ICE(2.5),
    MAGMA(3.0),
}

public fun ServerPlayer.getOnConductiveSurface(): ConductiveSurface? {
    val on = blockStateOn
    if (on.`is`(Blocks.MAGMA_BLOCK)) return ConductiveSurface.MAGMA
    if (on.`is`(BlockTags.SNOW)) return ConductiveSurface.SNOW
    if (on.`is`(BlockTags.BASE_STONE_OVERWORLD)) return ConductiveSurface.STONE
    if (on.`is`(METAL)) return ConductiveSurface.METAL
    if (on.`is`(BlockTags.ICE)) return ConductiveSurface.ICE
    return null
}

public enum class ConductiveMedium(public val conductance: Double) {
    RAIN(5.0),
    WATER(25.0),
    POWDER_SNOW(50.0),
    LAVA(1000.0),
}

public fun ServerPlayer.getInConductiveMedium(): ConductiveMedium? {
    if (this.isInLava) return ConductiveMedium.LAVA
    if (this.isInWater) return ConductiveMedium.WATER
    if (this.isInPowderSnow) return ConductiveMedium.POWDER_SNOW
    if (this.isInWaterOrRain) return ConductiveMedium.RAIN
    return null
}