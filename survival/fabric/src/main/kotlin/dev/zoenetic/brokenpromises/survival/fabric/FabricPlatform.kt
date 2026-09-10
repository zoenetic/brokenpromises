package dev.zoenetic.brokenpromises.survival.fabric

import dev.zoenetic.brokenpromises.survival.Survival.MOD_ID
import dev.zoenetic.brokenpromises.survival.platform.*
import dev.zoenetic.brokenpromises.survival.state.HeatSourceIndex
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.Identifier

public object FabricPlatform : Platform {
    override val name: PlatformName = PlatformName.FABRIC

    override val isDevelopmentEnvironment: Boolean
        get() = FabricLoader.getInstance().isDevelopmentEnvironment

    override fun isModLoaded(modId: String): Boolean = FabricLoader.getInstance().isModLoaded(modId)

    override val heatSources: ChunkView<HeatSourceIndex> = FabricChunkView(
        AttachmentRegistry.create(id(Store.CHUNK_HEAT_SOURCES))
        { it.initializer(::HeatSourceIndex) })

    override val playerConditions: PlayerStore<PlayerConditions> = FabricPlayerStore(
        AttachmentRegistry.create(id(Store.PLAYER_CONDITIONS))
        { it.initializer(::PlayerConditions) })

    override val vitals: SyncedPlayerStore<Vitals> = FabricSyncedPlayerStore(
        AttachmentRegistry.create(id(Store.PLAYER_VITALS))
        {
            it.initializer { Vitals.DEFAULT }
                .syncWith(Vitals.STREAM_CODEC, AttachmentSyncPredicate.all())
                .persistent(Vitals.CODEC)
        })
}

private fun id(store: Store) = Identifier.fromNamespaceAndPath(MOD_ID, store.id())
