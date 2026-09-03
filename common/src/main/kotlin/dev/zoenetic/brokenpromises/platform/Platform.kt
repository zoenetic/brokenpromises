package dev.zoenetic.brokenpromises.platform

public interface Platform {
    public val name: String

    public val isDevelopmentEnvironment: Boolean

   public  fun isModLoaded(modId: String): Boolean
}
