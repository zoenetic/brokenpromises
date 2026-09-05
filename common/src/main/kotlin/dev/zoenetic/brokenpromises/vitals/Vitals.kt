package dev.zoenetic.brokenpromises.vitals

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.zoenetic.brokenpromises.BrokenPromises
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer

public data class Vitals(val temperature: BodyTemperature) {
    public companion object {
        public val CODEC: Codec<Vitals> = RecordCodecBuilder.create { instance ->
            instance.group(
                BodyTemperature.CODEC.fieldOf("temperature").forGetter(Vitals::temperature),
            ).apply(instance, ::Vitals)
        }
        public val STREAM_CODEC: StreamCodec<ByteBuf, Vitals> =
            StreamCodec.composite(BodyTemperature.STREAM_CODEC, Vitals::temperature, ::Vitals)
        public val DEFAULT: Vitals = Vitals(BodyTemperature(37.0))
    }
}

public data class BodyTemperature(val value: Double) {
    public companion object {
        public val CODEC: Codec<BodyTemperature> = Codec.DOUBLE.xmap(::BodyTemperature, BodyTemperature::value)
        public val STREAM_CODEC: StreamCodec<ByteBuf, BodyTemperature> = ByteBufCodecs.DOUBLE.map(::BodyTemperature , BodyTemperature::value)
        public val DEFAULT: BodyTemperature = BodyTemperature(37.0)
    }
}

public fun ServerPlayer.setBodyTemperature(temperature: Double) {
    val vitals = BrokenPromises.platform.vitals(this)
    val newVitals = vitals.copy(temperature = BodyTemperature(temperature))
    BrokenPromises.platform.setVitals(this, newVitals)
}