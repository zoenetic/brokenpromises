package dev.zoenetic.brokenpromises

import dev.zoenetic.brokenpromises.platform.Platform
import org.slf4j.LoggerFactory

/**
 * Loader-agnostic entry point. Each loader module calls [init] with its own
 * [Platform] implementation; everything below this line stays vanilla-only.
 */
object BrokenPromises {

    const val MOD_ID = "brokenpromises"

    @JvmField
    val LOGGER = LoggerFactory.getLogger(MOD_ID)

    lateinit var platform: Platform
        private set

    fun init(platform: Platform) {
        this.platform = platform
        LOGGER.info("Broken Promises starting on {} (Minecraft 26.2)", platform.name)
    }
}
