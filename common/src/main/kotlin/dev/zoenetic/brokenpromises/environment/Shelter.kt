package dev.zoenetic.brokenpromises.environment

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.lighting.LightEngine

@JvmInline
public value class Sky(public val openness: Double) {
    public companion object {
        public fun fromBrightness(d: Double): Sky = Sky(d / LightEngine.MAX_LEVEL)
    }
}

public data class Shelter(
    val sky: Sky,
    val underOpenSky: Boolean,
)

public fun ServerPlayer.getShelter(): Shelter {
    val sky = getSky()
    val underOpenSky = isUnderOpenSky()
    return Shelter(sky, underOpenSky)
}

public fun ServerPlayer.getSky(): Sky {
    val level = level()
    val pos = blockPosition()
    val brightness = level.getBrightness(LightLayer.SKY, pos).toDouble()
    return Sky.fromBrightness(brightness)
}

public fun ServerPlayer.isUnderOpenSky(): Boolean {
    val level = level()
    val pos = blockPosition()
    return level.canSeeSky(pos)
}