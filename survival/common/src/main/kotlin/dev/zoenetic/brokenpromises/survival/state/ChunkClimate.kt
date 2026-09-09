package dev.zoenetic.brokenpromises.survival.state

import dev.zoenetic.brokenpromises.survival.probe.getClimate
import dev.zoenetic.brokenpromises.survival.sim.ClimateSample
import net.minecraft.world.level.chunk.LevelChunk

public object ChunkClimate {
    public fun of(chunk: LevelChunk): ClimateSample = chunk.getClimate()
}