package dev.zoenetic.brokenpromises.survival

import dev.zoenetic.brokenpromises.survival.platform.Platform
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public object Survival {

    public const val MOD_ID: String = "brokenpromises_survival"
    public const val NAMESPACE: String = "brokenpromises"

    @JvmField
    public val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    public lateinit var platform: Platform
        private set

    public fun init(platform: Platform) {
        this.platform = platform
        LOGGER.info("Broken Promises: Survival starting on {} (Minecraft 26.2)", platform.name)
    }
}
