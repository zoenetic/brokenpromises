package dev.zoenetic.brokenpromises.survival.neoforge

import dev.zoenetic.brokenpromises.survival.Survival.MOD_ID
import dev.zoenetic.brokenpromises.survival.platform.*
import dev.zoenetic.brokenpromises.survival.state.HeatSourceIndex
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import java.util.function.Supplier

public object NeoForgePlatform : Platform {
    override val name: PlatformName = PlatformName.NEOFORGE

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
        ATTACHMENTS.register(Store.CHUNK_HEAT_SOURCES.id(), Supplier {
            AttachmentType.builder(
                Supplier { HeatSourceIndex() }
            ).build()
        })
    )

    override val playerConditions: PlayerStore<PlayerConditions> =
        NeoForgePlayerStore(
            ATTACHMENTS.register(Store.PLAYER_CONDITIONS.id(), Supplier {
                AttachmentType.builder(Supplier { PlayerConditions() })
                    .build()
            })
        )

    override val vitals: SyncedPlayerStore<Vitals> = NeoForgeSyncedPlayerStore(
        ATTACHMENTS.register(Store.PLAYER_VITALS.id(), Supplier {
            AttachmentType.builder(Supplier { Vitals.DEFAULT })
                .serialize(Vitals.CODEC.fieldOf("vitals"))
                .sync(Vitals.STREAM_CODEC)
                .build()
        })
    )
}