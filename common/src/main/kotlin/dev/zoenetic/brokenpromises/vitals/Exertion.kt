package dev.zoenetic.brokenpromises.vitals

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer

internal const val EXERTION_WHEN_IDLE: Double = 0.0

public data class MovementPerSecond(
    public val pos: BlockPos,
    public val climbed: Double,
    public val jumped: Double,
    public val sprinted: Double,
    public val swum: Double,
    public val walked: Double,
)

@JvmInline
public value class Exertion(public val value: Double) {
    public companion object {
        public val CODEC: Codec<Exertion> =
            Codec.DOUBLE.xmap(
                ::Exertion,
                Exertion::value,
            )
        public val STEAM_CODEC: StreamCodec<ByteBuf, Exertion> =
            ByteBufCodecs.DOUBLE.map(
                ::Exertion,
                Exertion::value,
            )
        public val DEFAULT: Exertion =
            Exertion(EXERTION_WHEN_IDLE)
    }
}

public fun ServerPlayer.getNewExertion(elapsed: Long): Exertion {
    TODO()
}