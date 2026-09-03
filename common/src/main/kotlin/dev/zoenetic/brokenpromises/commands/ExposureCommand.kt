package dev.zoenetic.brokenpromises.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.zoenetic.brokenpromises.environment.getClimate
import dev.zoenetic.brokenpromises.environment.getConditions
import dev.zoenetic.brokenpromises.environment.getExposure
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import java.util.function.Supplier

public val exposureCommand: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("exposure")
    .executes { context ->
        val source = context.source
        val player = source.playerOrException
        val climate = player.getClimate()
        val conditions = player.getConditions(climate)
        val exposure = player.getExposure(conditions)
        source.sendSuccess({
            Component.literal("Exposure: ${exposure.temperature}")
        }, false)
        return@executes 1
    }

