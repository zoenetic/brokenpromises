package dev.zoenetic.unbidden.survival.debug

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.zoenetic.unbidden.survival.Survival.NAMESPACE
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

public val rootCommand: LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal(NAMESPACE)

public val survivalCommand: LiteralArgumentBuilder<CommandSourceStack> =
    Commands.literal("survival")
