package dev.zoenetic.brokenpromises.survival.neoforge

import dev.zoenetic.brokenpromises.survival.platform.*
import dev.zoenetic.brokenpromises.survival.state.HeatSourceIndex
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.neoforge.attachment.AttachmentType
import java.util.function.Supplier

public object NeoForgePlatform : Platform {
    override val name: String = "neoforge"

    override val isDevelopmentEnvironment: Boolean
        get() = !FMLLoader.getCurrent().isProduction

    override fun isModLoaded(modId: String): Boolean =
        FMLLoader.getCurrent().getLoadingModList()
            .getModFileById(modId) != null

    override val registrar: NeoForgeRegistrar = NeoForgeRegistrar

    override val heatSources: ChunkView<HeatSourceIndex> = NeoForgeChunkView(
        registrar.attachment("chunk_heat_sources") {
            AttachmentType.builder(Supplier { HeatSourceIndex() }).build()
        }
    )

    override val playerConditions: PlayerStore<PlayerConditions> =
        NeoForgePlayerStore(
            registrar.attachment("player_conditions") {
                AttachmentType.builder(Supplier { PlayerConditions.EMPTY }).build()
            }
        )

    override val vitals: SyncedPlayerStore<Vitals> = NeoForgeSyncedPlayerStore(
        registrar.attachment("vitals") {
            AttachmentType.builder(Supplier { Vitals.DEFAULT })
                .serialize(Vitals.CODEC.fieldOf("vitals"))
                .sync(Vitals.STREAM_CODEC)
                .build()
        }
    )

    public fun init(bus: IEventBus) {
        registrar.init(bus)
    }
}