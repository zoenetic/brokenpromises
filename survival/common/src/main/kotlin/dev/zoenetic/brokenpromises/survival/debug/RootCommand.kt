package dev.zoenetic.brokenpromises.survival.debug

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.zoenetic.brokenpromises.survival.Survival.NAMESPACE
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

public val rootCommand: LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal(NAMESPACE)

public val survivalCommand: LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("survival")
