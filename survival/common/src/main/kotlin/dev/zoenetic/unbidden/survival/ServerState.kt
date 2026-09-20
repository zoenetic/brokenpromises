package dev.zoenetic.unbidden.survival

import dev.zoenetic.unbidden.survival.fire.FireAttempts

public class ServerState {
    private var FIRE_ATTEMPTS: FireAttempts? = null

    public fun onServerStarting() {
        FIRE_ATTEMPTS = FireAttempts()
    }

    public fun onServerStopped() {
        FIRE_ATTEMPTS = null
    }

    public fun fireAttempts(): FireAttempts =
        FIRE_ATTEMPTS ?: error("accessed fire attempts state outside a running server")
}