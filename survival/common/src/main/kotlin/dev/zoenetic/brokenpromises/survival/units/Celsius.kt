package dev.zoenetic.brokenpromises.survival.units

import com.mojang.serialization.Codec
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

@JvmInline
public value class Celsius(public val value: Double) {

    public operator fun plus(d: TemperatureDifference): Celsius = Celsius(value + d.value)

    public operator fun minus(o: Celsius): TemperatureDifference =
        TemperatureDifference(value - o.value)

    public operator fun minus(d: TemperatureDifference): Celsius = Celsius(value - d.value)

    public operator fun compareTo(o: Celsius): Int = value.compareTo(o.value)

    public fun toDouble(): Double = value

    public companion object {
        public val CODEC: Codec<Celsius> =
            Codec.DOUBLE.xmap(
                ::Celsius,
                Celsius::value
            )
        public val STREAM_CODEC: StreamCodec<ByteBuf, Celsius> =
            ByteBufCodecs.DOUBLE.map(
                ::Celsius,
                Celsius::value
            )
    }
}