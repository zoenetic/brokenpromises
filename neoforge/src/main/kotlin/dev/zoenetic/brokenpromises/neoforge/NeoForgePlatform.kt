package dev.zoenetic.brokenpromises.neoforge

import dev.zoenetic.brokenpromises.BrokenPromises.MOD_ID
import dev.zoenetic.brokenpromises.platform.Platform
import dev.zoenetic.brokenpromises.vitals.Vitals
import net.minecraft.server.level.ServerPlayer
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

    override fun vitals(player: ServerPlayer): Vitals =
        player.getData(VITALS)

    override fun setVitals(
        player: ServerPlayer,
        value: Vitals
    ) {
        player.setData(VITALS, value)
    }
}
