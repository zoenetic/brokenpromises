package dev.zoenetic.brokenpromises.survival.neoforge

import dev.zoenetic.brokenpromises.survival.conditions.PlayerConditions
import dev.zoenetic.brokenpromises.survival.platform.ChunkView
import dev.zoenetic.brokenpromises.survival.platform.Platform
import dev.zoenetic.brokenpromises.survival.platform.PlayerStore
import dev.zoenetic.brokenpromises.survival.platform.SyncedPlayerStore
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
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

    override val register: NeoForgeRegister = NeoForgeRegister

    override val emitters: ChunkView<Long2ObjectOpenHashMap<Long>> = NeoForgeChunkView(
        register.attachment("chunk_emitters") {
            AttachmentType.builder(Supplier { Long2ObjectOpenHashMap<Long>() }).build()
        }
    )

    override val playerConditions: PlayerStore<PlayerConditions> =
        NeoForgePlayerStore(
            register.attachment("player_conditions") {
                AttachmentType.builder(Supplier { PlayerConditions.EMPTY }).build()
            }
        )

    override val vitals: SyncedPlayerStore<Vitals> = NeoForgeSyncedPlayerStore(
        register.attachment("vitals") {
            AttachmentType.builder(Supplier { Vitals.DEFAULT })
                .serialize(Vitals.CODEC.fieldOf("vitals"))
                .sync(Vitals.STREAM_CODEC)
                .build()
        }
    )

    public fun init(bus: IEventBus) {
        register.init(bus)
    }
}