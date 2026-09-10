package dev.zoenetic.brokenpromises.survival.neoforge

import dev.zoenetic.brokenpromises.survival.Survival.MOD_ID
import dev.zoenetic.brokenpromises.survival.platform.ChunkView
import dev.zoenetic.brokenpromises.survival.platform.Platform
import dev.zoenetic.brokenpromises.survival.platform.PlayerStore
import dev.zoenetic.brokenpromises.survival.platform.SyncedPlayerStore
import dev.zoenetic.brokenpromises.survival.state.HeatSourceIndex
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier

public object NeoForgePlatform : Platform {
    override val name: String = "neoforge"

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

    override val heatSources: ChunkView<HeatSourceIndex> = NeoForgeChunkView(
        ATTACHMENTS.register("chunk_heat_sources", Supplier {
            AttachmentType.builder(
                Supplier { HeatSourceIndex() }
            ).build()
        })
    )

    override val playerConditions: PlayerStore<PlayerConditions> =
        NeoForgePlayerStore(
            ATTACHMENTS.register("player_conditions", Supplier {
                AttachmentType.builder(Supplier { PlayerConditions.EMPTY })
                    .build()
            })
        )

    override val vitals: SyncedPlayerStore<Vitals> = NeoForgeSyncedPlayerStore(
        ATTACHMENTS.register("vitals", Supplier {
            AttachmentType.builder(Supplier { Vitals.DEFAULT })
                .serialize(Vitals.CODEC.fieldOf("vitals"))
                .sync(Vitals.STREAM_CODEC)
                .build()
        })
    )
}