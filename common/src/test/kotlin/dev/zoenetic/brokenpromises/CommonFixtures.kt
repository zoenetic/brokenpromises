package dev.zoenetic.brokenpromises

import dev.zoenetic.brokenpromises.BrokenPromises
import dev.zoenetic.brokenpromises.platform.Platform
import dev.zoenetic.brokenpromises.vitals.Vitals
import net.minecraft.SharedConstants
import net.minecraft.world.entity.player.Player
import java.util.IdentityHashMap
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.IdMapper
import net.minecraft.core.registries.Registries
import net.minecraft.data.registries.VanillaRegistries
import net.minecraft.resources.RegistryFixedCodec
import net.minecraft.server.Bootstrap
import net.minecraft.world.level.ChunkPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.chunk.PalettedContainer
import net.minecraft.world.level.chunk.PalettedContainerFactory
import net.minecraft.world.level.chunk.Strategy
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock

/** A Platform for tests: no loader, vitals kept in a map keyed by player identity. */
object TestPlatform : Platform {
    override val name: String = "Test"
    override val isDevelopmentEnvironment: Boolean = false
    override fun isModLoaded(modId: String): Boolean = false

    private val vitals = IdentityHashMap<Player, Vitals>()
    override fun vitals(player: Player): Vitals = vitals.getOrPut(player) { Vitals.DEFAULT }
    override fun setVitals(player: Player, value: Vitals) { vitals[player] = value }
}

object CommonFixtures {
    init {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        BrokenPromises.init(TestPlatform)
    }

    const val MIN_Y = -64
    const val HEIGHT = 384

    private val lookup: HolderLookup.Provider by lazy { VanillaRegistries.createLookup() }

    private val containerFactory: PalettedContainerFactory by lazy {
        val blockStrategy = Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY)
        val air = Blocks.AIR.defaultBlockState()
        val plains: Holder<Biome> = lookup.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS)
        val biomeIds = IdMapper<Holder<Biome>>().apply { add(plains) }
        val biomeStrategy = Strategy.createForBiomes(biomeIds)
        PalettedContainerFactory(
            blockStrategy,
            air,
            PalettedContainer.codecRW(BlockState.CODEC, blockStrategy, air),
            biomeStrategy,
            plains,
            PalettedContainer.codecRO(RegistryFixedCodec.create(Registries.BIOME), biomeStrategy, plains),
        )
    }

    fun fakeLevel(): Level {
        val level = mock(Level::class.java, CALLS_REAL_METHODS)
        doReturn(HEIGHT).`when`(level).height
        doReturn(MIN_Y).`when`(level).minY
        doReturn(false).`when`(level).isClientSide
        doReturn(containerFactory).`when`(level).palettedContainerFactory()
        return level
    }

    fun chunk(level: Level, pos: ChunkPos = ChunkPos(16, 32)): LevelChunk = LevelChunk(level, pos)

    /** Same as [fakeLevel] but a ServerLevel, for code that needs the server type. Same four stubs. */
    fun fakeServerLevel(): ServerLevel {
        val level = mock(ServerLevel::class.java, CALLS_REAL_METHODS)
        doReturn(HEIGHT).`when`(level).height
        doReturn(MIN_Y).`when`(level).minY
        doReturn(false).`when`(level).isClientSide
        doReturn(containerFactory).`when`(level).palettedContainerFactory()
        return level
    }
}
