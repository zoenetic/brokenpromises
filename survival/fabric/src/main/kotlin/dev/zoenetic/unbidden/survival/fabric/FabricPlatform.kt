package dev.zoenetic.unbidden.survival.fabric

import dev.zoenetic.unbidden.survival.Survival.MOD_ID
import dev.zoenetic.unbidden.survival.conditions.PlayerConditions
import dev.zoenetic.unbidden.survival.platform.ChunkView
import dev.zoenetic.unbidden.survival.platform.Platform
import dev.zoenetic.unbidden.survival.platform.PlayerStore
import dev.zoenetic.unbidden.survival.platform.SyncedPlayerStore
import dev.zoenetic.unbidden.survival.vitals.Vitals
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.Identifier

public object FabricPlatform : Platform {
    override val name: String = "fabric"
    override val isDevelopmentEnvironment: Boolean
        get() = FabricLoader.getInstance().isDevelopmentEnvironment

    override fun isModLoaded(modId: String): Boolean = FabricLoader.getInstance().isModLoaded(modId)

    override val register: FabricRegister = FabricRegister

    override val emitters: ChunkView<Long2ObjectOpenHashMap<Long>> = FabricChunkView(
        AttachmentRegistry.create(id("chunk_emitters")) {
            it.initializer { Long2ObjectOpenHashMap<Long>() }
        }
    )

    override val playerConditions: PlayerStore<PlayerConditions> = FabricPlayerStore(
        AttachmentRegistry.create(id("player_conditions"))
    )

    override val vitals: SyncedPlayerStore<Vitals> = FabricSyncedPlayerStore(
        AttachmentRegistry.create(id("vitals"))
        {
            it.initializer { Vitals.DEFAULT }
                .syncWith(Vitals.STREAM_CODEC, AttachmentSyncPredicate.all())
                .persistent(Vitals.CODEC)
        })

}

private fun id(path: String) = Identifier.fromNamespaceAndPath(MOD_ID, path)
