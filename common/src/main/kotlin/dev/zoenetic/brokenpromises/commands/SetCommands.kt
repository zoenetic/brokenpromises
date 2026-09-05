package dev.zoenetic.brokenpromises.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.zoenetic.brokenpromises.vitals.setBodyTemperature
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

public val setBodyTemperatureCommand: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("setBodyTemperature")
    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
    .then(Commands.argument("bodyTemperature", DoubleArgumentType.doubleArg())
        .executes { context ->
            val temperature = DoubleArgumentType.getDouble(context, "bodyTemperature")
            context.source.playerOrException.setBodyTemperature(temperature)
            1
        })