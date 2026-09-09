package dev.zoenetic.brokenpromises

import dev.zoenetic.brokenpromises.commands.BrokenPromisesWatchers
import dev.zoenetic.brokenpromises.environment.BrokenPromisesChunks
import dev.zoenetic.brokenpromises.platform.Platform
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class BrokenPromises(
    public var chunks: BrokenPromisesChunks,
    public var players: BrokenPromisesPlayers,
    public var servers: BrokenPromisesServers,
    public var watchers: BrokenPromisesWatchers,
) {

    public fun get() {
        TODO()
    }

    public fun set() {
        TODO()
    }

    public fun tick() {
        TODO()
    }

    public companion object {

        public const val MOD_ID: String = "brokenpromises"

        @JvmField
        public val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

        public lateinit var platform: Platform
            private set

        public fun init(platform: Platform): BrokenPromises {
            this.platform = platform
            LOGGER.info("Broken Promises starting on {} (Minecraft 26.2)", platform.name)
            return BrokenPromises(
                BrokenPromisesChunks.new(),
                BrokenPromisesPlayers.new(),
                BrokenPromisesServers.new(),
                BrokenPromisesWatchers.new(),
            )
        }
    }
}