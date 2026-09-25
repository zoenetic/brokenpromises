package dev.zoenetic.unbidden.survival.platform

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.chunk.LevelChunk

public fun LevelChunk.isSendable(): Boolean {
    val key = pos.pack()
    val serverLevel = level as? ServerLevel ?: return false
    return serverLevel.chunkSource.chunkMap.getChunkToSend(key) != null
}