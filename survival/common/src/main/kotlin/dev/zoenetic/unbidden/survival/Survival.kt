package dev.zoenetic.unbidden.survival

import dev.zoenetic.unbidden.survival.conditions.PlayerConditions
import dev.zoenetic.unbidden.survival.platform.Platform
import dev.zoenetic.unbidden.survival.registry.UnbiddenBlocks
import dev.zoenetic.unbidden.survival.registry.UnbiddenItems
import dev.zoenetic.unbidden.survival.registry.UnbiddenSounds
import dev.zoenetic.unbidden.survival.vitals.BreathParticles
import dev.zoenetic.unbidden.survival.vitals.Exertion
import dev.zoenetic.unbidden.survival.vitals.SpeedPenalty
import dev.zoenetic.unbidden.survival.vitals.Vitals
import net.minecraft.server.level.ServerLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public object Survival {

    public const val MOD_ID: String = "unbidden_survival"
    public const val NAMESPACE: String = "unbidden"

    @JvmField
    public val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    public lateinit var platform: Platform
        private set

    public lateinit var serverState: ServerState
        private set

    public fun init(platform: Platform) {
        this.platform = platform
        UnbiddenBlocks.init()
        UnbiddenItems.init()
        UnbiddenSounds.init()
        LOGGER.info(
            "Unbidden: Survival (server) starting on {} (Minecraft 26.2)",
            platform.name
        )
    }

    public fun tick(level: ServerLevel) {
        PlayerConditions.tick(level)
        Vitals.tick(level)
        Exertion.tick(level)
        SpeedPenalty.tick(level)
        BreathParticles.tick(level)
    }
}
