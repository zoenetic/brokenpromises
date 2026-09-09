package dev.zoenetic.brokenpromises.survival.neoforge

import dev.zoenetic.brokenpromises.survival.Survival.MOD_ID
import dev.zoenetic.brokenpromises.survival.platform.Platform
import dev.zoenetic.brokenpromises.survival.platform.SyncedPlayerStore
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier

public object NeoForgePlatform : Platform {
    override val name: String = "NeoForge"

    override val isDevelopmentEnvironment: Boolean
        get() = !FMLLoader.getCurrent().isProduction

    override fun isModLoaded(modId: String): Boolean =
        FMLLoader.getCurrent().getLoadingModList()
            .getModFileById(modId) != null

    internal val ATTACHMENTS: DeferredRegister<AttachmentType<*>> =
        DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES,
            MOD_ID
        )

    private val VITALS: DeferredHolder<AttachmentType<*>, AttachmentType<Vitals>> =
        ATTACHMENTS.register("vitals", Supplier {
            AttachmentType.builder(Supplier { Vitals.DEFAULT })
                .serialize(Vitals.CODEC.fieldOf("vitals"))
                .sync(Vitals.STREAM_CODEC)
                .build()
        })

    // Synced to every client tracking the player, not just the player themselves:
    // LivingEntityRenderer draws other players shivering too.
    override val vitals: SyncedPlayerStore<Vitals> = NeoForgeSyncedPlayerStore(VITALS)
}
