package dev.zoenetic.brokenpromises.survival

import dev.zoenetic.brokenpromises.survival.Survival
import dev.zoenetic.brokenpromises.survival.platform.ChunkView
import dev.zoenetic.brokenpromises.survival.platform.Platform
import dev.zoenetic.brokenpromises.survival.platform.PlayerStore
import dev.zoenetic.brokenpromises.survival.platform.SyncedPlayerStore
import dev.zoenetic.brokenpromises.survival.state.HeatSourceIndex
import dev.zoenetic.brokenpromises.survival.state.PlayerConditions
import dev.zoenetic.brokenpromises.survival.vitals.Vitals
import net.minecraft.SharedConstants
import net.minecraft.server.level.ServerPlayer
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

/** A Platform for tests: no loader, every store an identity map. */
object TestPlatform : Platform {
    override val name: String = "Test"
    override val isDevelopmentEnvironment: Boolean = false
    override fun isModLoaded(modId: String): Boolean = false

    override val heatSources: ChunkView<HeatSourceIndex> =
        object : ChunkView<HeatSourceIndex> {
            private val byChunk = IdentityHashMap<LevelChunk, HeatSourceIndex>()
            override fun get(chunk: LevelChunk): HeatSourceIndex =
                byChunk.getOrPut(chunk) { HeatSourceIndex() }
        }

    override val playerConditions: PlayerStore<PlayerConditions> =
        object : PlayerStore<PlayerConditions> {
            private val byPlayer = IdentityHashMap<Player, PlayerConditions>()
            override fun get(player: ServerPlayer): PlayerConditions =
                byPlayer.getOrPut(player) { PlayerConditions() }

            override fun set(player: ServerPlayer, value: PlayerConditions) {
                byPlayer[player] = value
            }
        }

    override val vitals: SyncedPlayerStore<Vitals> =
        object : SyncedPlayerStore<Vitals> {
            private val byPlayer = IdentityHashMap<Player, Vitals>()
            override fun get(player: Player): Vitals =
                byPlayer.getOrPut(player) { Vitals.DEFAULT }

            override fun set(player: ServerPlayer, value: Vitals) {
                byPlayer[player] = value
            }
        }
}

object CommonFixtures {
    init {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        Survival.init(TestPlatform)
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
