package dev.zoenetic.brokenpromises.survival.registry

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.platform.getValue
import net.minecraft.sounds.SoundEvent

public object BrokenPromisesSounds {

    public val BREATH: SoundEvent by Survival.platform.register.sound("breath") {
        SoundEvent.createFixedRangeEvent(it, 0F)
    }

    public val FIRE_FAILURE: SoundEvent by
    Survival.platform.register.sound("fire_failure") {
        SoundEvent.createVariableRangeEvent(it)
    }

    public val FIRE_SUCCESS: SoundEvent by
    Survival.platform.register.sound("fire_success") {
        SoundEvent.createVariableRangeEvent(it)
    }

    public val HEARTBEAT: SoundEvent by Survival.platform.register.sound("heartbeat") {
        SoundEvent.createFixedRangeEvent(it, 0F)
    }

    public fun init() {}
}
