package dev.zoenetic.brokenpromises.environment

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.lighting.LightEngine

@JvmInline
public value class Sky(public val openness: Double) {
    public companion object {
        public fun fromBrightness(d: Double): Sky = Sky(d / LightEngine.MAX_LEVEL)
    }
}

public class Shelter(
    public val sky: Sky,
    public val underOpenSky: Boolean,
) {
    public companion object {
        public fun get(level: ServerLevel, pos: BlockPos): Shelter {
            val sky = getSky(level, pos)
            val underOpenSky = isUnderOpenSky(level, pos)
            return Shelter(sky, underOpenSky)
        }

        public fun getSky(level: ServerLevel, pos: BlockPos): Sky {
            val brightness = level.getBrightness(LightLayer.SKY, pos).toDouble()
            return Sky.fromBrightness(brightness)
        }

        public fun isUnderOpenSky(level: ServerLevel, pos: BlockPos): Boolean {
            return level.canSeeSky(pos)
        }
    }
}