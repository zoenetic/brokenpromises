package dev.zoenetic.brokenpromises.vitals

import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.stats.Stats

public class MovementStatistics(
    public val climbing: Int,
    public val flying: Int,
    public val sneaking: Int,
    public val sprinting: Int,
    public val swimming: Int,
    public val walking: Int,
    public val walkingUnderWater: Int,
) {
    public companion object {
        public val DEFAULT: MovementStatistics =
            MovementStatistics(
                0, 0, 0, 0, 0, 0, 0
            )

    }
}

public fun ServerPlayer.getNewMovementStatistics(): MovementStatistics {
    val stat = { id: Identifier -> stats.getValue(Stats.CUSTOM.get(id)) }
    return MovementStatistics(
        climbing = stat(Stats.CLIMB_ONE_CM),
        flying = stat(Stats.FLY_ONE_CM),
        sneaking = stat(Stats.CROUCH_ONE_CM),
        sprinting = stat(Stats.SPRINT_ONE_CM),
        swimming = stat(Stats.SWIM_ONE_CM),
        walking = stat(Stats.WALK_ONE_CM),
        walkingUnderWater = stat(Stats.WALK_UNDER_WATER_ONE_CM),
    )
}

public data class MovementSpeed(
    public val climbing: Double,
    public val flying: Double,
    public val sneaking: Double,
    public val sprinting: Double,
    public val swimming: Double,
    public val walking: Double,
    public val walkingUnderWater: Double,
)

