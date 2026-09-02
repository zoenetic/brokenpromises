package dev.zoenetic.brokenpromises.platform

/**
 * The seam between common code and the loaders. Implemented once per loader
 * module and handed to [dev.zoenetic.brokenpromises.BrokenPromises.init].
 *
 * Architectury's `@ExpectPlatform` is not an option on 26.x - Architectury Loom
 * cannot build unobfuscated versions - so constructor injection it is.
 */
interface Platform {
    /** Human-readable loader name, e.g. "Fabric" or "NeoForge". */
    val name: String

    val isDevelopmentEnvironment: Boolean

    fun isModLoaded(modId: String): Boolean
}
