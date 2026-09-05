package dev.zoenetic.brokenpromises.fabric

import dev.zoenetic.brokenpromises.BrokenPromises.MOD_ID
import dev.zoenetic.brokenpromises.vitals.BodyTemperature
import dev.zoenetic.brokenpromises.platform.Platform
import dev.zoenetic.brokenpromises.vitals.Vitals
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer

public object FabricPlatform : Platform {
    override val name: String = "Fabric"

    override val isDevelopmentEnvironment: Boolean
        get() = FabricLoader.getInstance().isDevelopmentEnvironment

    override fun isModLoaded(modId: String): Boolean =
        FabricLoader.getInstance().isModLoaded(modId)

    private val VITALS: AttachmentType<Vitals> = AttachmentRegistry.create(Identifier.fromNamespaceAndPath(MOD_ID, "vitals")) { builder ->
        builder
            .persistent(Vitals.CODEC)
            .initializer { Vitals.DEFAULT }
            .syncWith(Vitals.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    }

    override fun vitals(player: ServerPlayer): Vitals = player.getAttachedOrCreate(VITALS)

    override fun setVitals(
        player: ServerPlayer,
        value: Vitals
    ) {
        player.setAttached(VITALS, value)
    }
}
