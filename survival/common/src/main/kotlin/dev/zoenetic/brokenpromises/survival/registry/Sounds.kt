package dev.zoenetic.brokenpromises.survival.registry

import dev.zoenetic.brokenpromises.survival.Survival
import net.minecraft.core.Holder
import net.minecraft.sounds.SoundEvent

public object Sounds {

    public val HEARTBEAT: Holder<SoundEvent> = Survival.platform.registrar.sound("heartbeat") {
        SoundEvent.createFixedRangeEvent(it, 0F)
    }

    public val BREATH: Holder<SoundEvent> = Survival.platform.registrar.sound("breath") {
        SoundEvent.createFixedRangeEvent(it, 0F)
    }

    public fun init() {}
}
