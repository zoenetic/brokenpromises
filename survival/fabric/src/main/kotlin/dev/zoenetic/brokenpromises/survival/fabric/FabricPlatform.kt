package dev.zoenetic.brokenpromises.survival.fabric

import dev.zoenetic.brokenpromises.survival.Survival.MOD_ID
import dev.zoenetic.brokenpromises.survival.platform.ChunkView
import dev.zoenetic.brokenpromises.survival.platform.Platform
import dev.zoenetic.brokenpromises.survival.platform.PlayerStore
import dev.zoenetic.brokenpromises.survival.platform.SyncedPlayerStore
import dev.zoenetic.brokenpromises.survival.state.HeatSourceIndex
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.Identifier

public object FabricPlatform : Platform {
    override val name: String = "Fabric"

    override val isDevelopmentEnvironment: Boolean
        get() = FabricLoader.getInstance().isDevelopmentEnvironment

    override fun isModLoaded(modId: String): Boolean = FabricLoader.getInstance().isModLoaded(modId)

    override val heatSources: ChunkView<HeatSourceIndex> = FabricChunkView(
        AttachmentRegistry.create(id("chunk_heat_sources"))
        { it.initializer(::HeatSourceIndex) })

    override val playerConditions: PlayerStore<PlayerConditions> = FabricPlayerStore(
        AttachmentRegistry.create(id("player_conditions"))
        { it.initializer(::PlayerConditions) })

    // Synced to every client tracking the player, not just the player themselves:
    // LivingEntityRenderer draws other players shivering too.
    override val vitals: SyncedPlayerStore<Vitals> = FabricSyncedPlayerStore(
        AttachmentRegistry.create(id("player_vitals"))
        {
            it.initializer { Vitals.DEFAULT }
                .syncWith(Vitals.STREAM_CODEC, AttachmentSyncPredicate.all())
        })
}

private fun id(path: String) = Identifier.fromNamespaceAndPath(MOD_ID, path)
