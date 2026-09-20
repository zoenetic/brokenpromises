package dev.zoenetic.unbidden.survival.registry

import dev.zoenetic.unbidden.survival.Survival
import dev.zoenetic.unbidden.survival.platform.getValue
import net.minecraft.sounds.SoundEvent

public object UnbiddenSounds {

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
