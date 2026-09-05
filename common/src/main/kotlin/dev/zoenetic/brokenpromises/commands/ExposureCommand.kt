package dev.zoenetic.brokenpromises.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.zoenetic.brokenpromises.environment.getVitals
import dev.zoenetic.brokenpromises.environment.getClimate
import dev.zoenetic.brokenpromises.environment.getConditions
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

public val exposureCommand: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("exposure")
    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
    .executes { context ->
        val source = context.source
        val player = source.playerOrException
        val climate = player.getClimate()
        val conditions = player.getConditions(climate)
        val airTemperature = conditions.temperature.value
        val vitals = player.getVitals()
        val bodyTemperature = vitals.temperature.value
        source.sendSuccess({
            Component.literal("Air: ${String.format("%.1f", airTemperature)} C, Body: ${String.format("%.1f", bodyTemperature)} C")
        }, false)
        return@executes 1
    }

