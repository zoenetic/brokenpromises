package dev.zoenetic.brokenpromises.survival

import dev.zoenetic.brokenpromises.survival.effects.player.BreathParticles
import dev.zoenetic.brokenpromises.survival.effects.player.SpeedPenalty
import dev.zoenetic.brokenpromises.survival.platform.Platform
import dev.zoenetic.brokenpromises.survival.registry.BrokenPromisesBlocks
import dev.zoenetic.brokenpromises.survival.registry.BrokenPromisesItems
import dev.zoenetic.brokenpromises.survival.registry.BrokenPromisesSounds
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.state.ServerState
import dev.zoenetic.brokenpromises.survival.vitals.Exertion
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.minecraft.server.level.ServerLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public object Survival {

    public const val MOD_ID: String = "brokenpromises_survival"
    public const val NAMESPACE: String = "brokenpromises"

    @JvmField
    public val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    public lateinit var platform: Platform
        private set

    public lateinit var serverState: ServerState
        private set

    public fun init(platform: Platform) {
        this.platform = platform
        BrokenPromisesBlocks.init()
        BrokenPromisesItems.init()
        BrokenPromisesSounds.init()
        LOGGER.info(
            "Broken Promises: Survival (server) starting on {} (Minecraft 26.2)",
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
