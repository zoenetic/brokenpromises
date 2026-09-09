package dev.zoenetic.brokenpromises.vitals

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.codec.StreamCodec


public class Vitals(
    private var bodyTemperature: BodyTemperature,
    private var heartRate: HeartRate,
) {

    public fun getBodyTemperature(): BodyTemperature {
        return bodyTemperature
    }

    public fun setBodyTemperature(temperature: BodyTemperature) {
        this.bodyTemperature = temperature
    }

    public val setBodyTemperatureCommand: LiteralArgumentBuilder<CommandSourceStack> =
        Commands.literal("setBodyTemperature")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(
                Commands.argument("bodyTemperature", DoubleArgumentType.doubleArg())
                    .executes { context ->
                        val value = DoubleArgumentType.getDouble(context, "bodyTemperature")
                        setBodyTemperature(BodyTemperature(value))
                        1
                    })

    public fun getHeartRate(elapsed: Long): HeartRate {
        val target = targetHeartRate()
        val current = heartRate
        if (current.value == target) return current
        val halfLifeSeconds = effectiveHalfLifeForHeartRate()
        return HeartRate(
            approachHeartRate(
                halfLifeSeconds,
            )
        )
    }

    public companion object {
        public val CODEC: Codec<Vitals> =
            RecordCodecBuilder.create { instance ->
                instance.group(
                    BodyTemperature.CODEC.fieldOf("temperature")
                        .forGetter(Vitals::bodyTemperature),
                    HeartRate.CODEC.fieldOf("heartRate")
                        .forGetter(Vitals::heartRate),
                ).apply(instance, ::Vitals)
            }
        public val STREAM_CODEC: StreamCodec<ByteBuf, Vitals> =
            StreamCodec.composite(
                BodyTemperature.STREAM_CODEC,
                Vitals::bodyTemperature,
                HeartRate.STREAM_CODEC,
                Vitals::heartRate,
                ::Vitals
            )
        public val DEFAULT: Vitals =
            Vitals(
                BodyTemperature(NORMAL_BODY_TEMPERATURE),
                HeartRate(RESTING_HEART_RATE),
            )
    }
}