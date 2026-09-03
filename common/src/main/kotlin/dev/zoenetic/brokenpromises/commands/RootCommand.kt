package dev.zoenetic.brokenpromises.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

public val rootCommand: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("brokenpromises")