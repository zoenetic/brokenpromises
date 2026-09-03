package dev.zoenetic.brokenpromises.environment

import dev.zoenetic.brokenpromises.heat.Temperature
import net.minecraft.server.level.ServerPlayer
import java.util.UUID

public data class Exposure(
    val temperature: Temperature,
)

public data class ExposureSample(
    val player: UUID,
    val tick: Long,
    val exposure: Exposure,
)

public fun ServerPlayer.getExposure(conditions: Conditions): Exposure {
    return Exposure(
        temperature = conditions.temperature,
    )
}