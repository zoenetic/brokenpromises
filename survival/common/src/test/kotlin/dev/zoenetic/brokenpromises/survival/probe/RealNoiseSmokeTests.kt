package dev.zoenetic.brokenpromises.survival.probe

import dev.zoenetic.brokenpromises.survival.CommonFixtures
import net.minecraft.world.level.ChunkPos
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RealNoiseSmokeTests {

    private val level = CommonFixtures.climateLevel()

    private fun at(x: Int, z: Int) = CommonFixtures.climateChunk(ChunkPos(x, z), level)

    @Test
    fun `humidity from real noise lands inside the unit range`() {
        for (x in 0..8) {
            val humidity = at(x, x * 3).getHumidity().value
            assertTrue(humidity in 0.0..1.0, "chunk ($x, ${x * 3}) gave humidity $humidity")
        }
    }

    @Test
    fun `base temperature from real noise stays between pole and equator`() {
        for (x in 0..8) {
            val t = at(x, -x).getBaseTemperature().value
            assertTrue(t >= POLE_C, "chunk ($x, ${-x}) gave $t, below $POLE_C")
            assertTrue(t <= EQUATOR_C, "chunk ($x, ${-x}) gave $t, above $EQUATOR_C")
        }
    }

    @Test
    fun `the climate really does vary from place to place`() {
        val temperatures = (0..40 step 4).map { at(it, it).getBaseTemperature().value }
        assertTrue(
            temperatures.distinct().size > 1,
            "real noise should differ across chunks, got $temperatures"
        )
    }

    @Test
    fun `the same chunk position always samples the same climate`() {
        val once = at(7, -13).getClimate()
        val twice = at(7, -13).getClimate()
        assertEquals(once, twice, "$once should equal $twice")
    }

    @Test
    fun `wind from real noise has a finite, non-negative speed`() {
        for (x in 0..8) {
            val speed = at(x, x + 5).getWind().speed
            assertTrue(speed.isFinite() && speed >= 0.0, "chunk gave wind speed $speed")
        }
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures.bootstrap()
        }
    }
}
