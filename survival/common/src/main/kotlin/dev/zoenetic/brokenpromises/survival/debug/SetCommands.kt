package dev.zoenetic.brokenpromises.survival.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.vitals.BodyTemperature
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

public val setBodyTemperatureCommand: LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("setBodyTemperature")
        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
        .then(
            Commands.argument("bodyTemperature", DoubleArgumentType.doubleArg())
                .executes { context ->
                    val temperature = DoubleArgumentType.getDouble(context, "bodyTemperature")
                    val bodyTemperature = BodyTemperature(Celsius(temperature))
                    val player = context.source.playerOrException
                    val vitals =
                        Survival.platform.vitals.get(player).copy(bodyTemperature = bodyTemperature)
                    Survival.platform.vitals.set(player, vitals)
                    1
                })