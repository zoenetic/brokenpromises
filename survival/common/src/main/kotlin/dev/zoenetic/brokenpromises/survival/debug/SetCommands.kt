package dev.zoenetic.brokenpromises.survival.debug

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.vitals.BodyTemperature
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

public val setBodyTemperatureCommand: LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("setBodyTemperature")
        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
        .then(
            Commands.argument("bodyTemperature", DoubleArgumentType.doubleArg())
                .executes { context ->
                    val temperature = DoubleArgumentType.getDouble(context, "bodyTemperature")
                    val bodyTemperature = BodyTemperature(Celsius(temperature))
                    val source = context.source
                    val player = source.playerOrException
                    val vitals = Survival.platform.vitals.get(player)
                    if (vitals == null) {
                        source.sendFailure(
                            Component.literal("Failed to set body temperature; player conditions not found")
                        )
                        return@executes 0
                    }
                    val newVitals = vitals.copy(bodyTemperature = bodyTemperature)
                    Survival.platform.vitals.set(player, newVitals)
                    1
                })