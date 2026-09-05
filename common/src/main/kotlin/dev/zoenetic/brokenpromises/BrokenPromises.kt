package dev.zoenetic.brokenpromises

import dev.zoenetic.brokenpromises.platform.Platform
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public object BrokenPromises {

    public const val MOD_ID: String = "brokenpromises"

    @JvmField
    public val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    public lateinit var platform: Platform
        private set

    public fun init(platform: Platform) {
        this.platform = platform
        LOGGER.info("Broken Promises starting on {} (Minecraft 26.2)", platform.name)
    }
}