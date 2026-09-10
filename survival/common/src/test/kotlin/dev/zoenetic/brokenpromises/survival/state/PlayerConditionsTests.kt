package dev.zoenetic.brokenpromises.survival.state

import dev.zoenetic.brokenpromises.survival.CommonFixtures
import dev.zoenetic.brokenpromises.survival.CommonFixtures.coldestSite
import dev.zoenetic.brokenpromises.survival.CommonFixtures.hottestSite
import dev.zoenetic.brokenpromises.survival.CommonFixtures.seaLevelCentreOf
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.world.level.ChunkPos
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PlayerConditionsTests {

    @Test
    fun `the climate chunk consulted is the one containing the player`() {
        val world = CommonFixtures.fakeWorld()
        val standingOn = BlockPos(1234, CommonFixtures.SEA_LEVEL, -5678)
        val player = world.playerAt(standingOn)

        val _ = PlayerConditions.get(player)

        val expected = ChunkPos(
            SectionPos.blockToSectionCoord(standingOn.x),
            SectionPos.blockToSectionCoord(standingOn.z),
        )
        assertEquals(
            expected,
            world.requestedChunks.first(),
            "conditions were read from ${world.requestedChunks.first()} " +
                    "but the player is standing in $expected"
        )
    }

    @Test
    fun `conditions report the climate of the player's own column`() {
        val world = CommonFixtures.fakeWorld()
        val player = world.playerAt(seaLevelCentreOf(hottestSite.pos))

        val conditions = PlayerConditions.get(player)

        assertEquals(
            hottestSite.temperature.value,
            conditions.temperature.value,
            1e-9,
            "player is in ${hottestSite.pos}, which the scan puts at " +
                    "${hottestSite.temperature.value}°C"
        )
    }

    @Test
    fun `standing somewhere hot reads hot`() {
        val world = CommonFixtures.fakeWorld()
        val conditions = PlayerConditions.get(world.playerAt(seaLevelCentreOf(hottestSite.pos)))
        assertTrue(
            conditions.temperature.value > 25.0,
            "the hottest column in the world read ${conditions.temperature.value}°C"
        )
    }

    @Test
    fun `standing somewhere cold reads cold`() {
        val world = CommonFixtures.fakeWorld()
        val conditions = PlayerConditions.get(world.playerAt(seaLevelCentreOf(coldestSite.pos)))
        assertTrue(
            conditions.temperature.value < 5.0,
            "the coldest column in the world read ${conditions.temperature.value}°C"
        )
    }

    @Test
    fun `moving between climates changes the reported temperature`() {
        val world = CommonFixtures.fakeWorld()
        val hot = PlayerConditions.get(world.playerAt(seaLevelCentreOf(hottestSite.pos)))
        val cold = PlayerConditions.get(world.playerAt(seaLevelCentreOf(coldestSite.pos)))
        assertTrue(
            hot.temperature.value > cold.temperature.value,
            "hot ${hot.temperature.value}°C should exceed cold ${cold.temperature.value}°C"
        )
    }

    @Test
    fun `altitude cools the reading above sea level`() {
        val world = CommonFixtures.fakeWorld()
        val ground = seaLevelCentreOf(hottestSite.pos)
        val atGround = PlayerConditions.get(world.playerAt(ground))
        val onAPeak = PlayerConditions.get(world.playerAt(ground.above(200)))
        assertTrue(
            onAPeak.temperature.value < atGround.temperature.value,
            "200 blocks up read ${onAPeak.temperature.value}°C " +
                    "against ${atGround.temperature.value}°C at sea level"
        )
    }

    @Test
    fun `an open sky admits a diurnal swing that a sealed one does not`() {
        val pos = seaLevelCentreOf(hottestSite.pos)
        val sealed = CommonFixtures.fakeWorld(skyBrightness = 0, clockTime = 9000L)
        val open = CommonFixtures.fakeWorld(skyBrightness = 15, clockTime = 9000L)
        assertNotEquals(
            PlayerConditions.get(sealed.playerAt(pos)).temperature.value,
            PlayerConditions.get(open.playerAt(pos)).temperature.value,
            "sky brightness should feed the diurnal swing"
        )
    }

    @Test
    fun `the diurnal swing follows the day clock, not total world age`() {
        val pos = seaLevelCentreOf(hottestSite.pos)
        val warmest = CommonFixtures.fakeWorld(
            skyBrightness = 15, gameTime = 500_000L, clockTime = 9000L
        )
        val coldest = CommonFixtures.fakeWorld(
            skyBrightness = 15, gameTime = 500_000L, clockTime = 21000L
        )
        assertTrue(
            PlayerConditions.get(warmest.playerAt(pos)).temperature.value >
                    PlayerConditions.get(coldest.playerAt(pos)).temperature.value,
            "mid-afternoon should beat pre-dawn even at the same game time"
        )
    }

    @Test
    fun `conditions record the monotonic game time, not the day clock`() {
        val world = CommonFixtures.fakeWorld(gameTime = 500_000L, clockTime = 9000L)
        val conditions = PlayerConditions.get(world.playerAt(seaLevelCentreOf(hottestSite.pos)))
        assertEquals(500_000L, conditions.time.value, "elapsed needs a clock that never rewinds")
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures.bootstrap()
        }
    }
}
