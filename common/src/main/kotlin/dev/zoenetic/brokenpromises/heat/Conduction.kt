package dev.zoenetic.brokenpromises.heat

import dev.zoenetic.brokenpromises.BrokenPromises.Companion.MOD_ID
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

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

public enum class ConductiveMedium(public val conductance: Double) {
    RAIN(5.0),
    WATER(25.0),
    POWDER_SNOW(50.0),
    LAVA(1000.0),
}