package dev.zoenetic.brokenpromises.survival.registry

import dev.zoenetic.brokenpromises.survival.Survival
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent

public object Sounds {
    public val HEARTBEAT_SOUND_ID: Identifier =
        Identifier.fromNamespaceAndPath(Survival.NAMESPACE, "heartbeat")
    public val HEARTBEAT_SOUND_EVENT: SoundEvent =
        SoundEvent.createFixedRangeEvent(HEARTBEAT_SOUND_ID, 0F)
}