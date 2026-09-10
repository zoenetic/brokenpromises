package dev.zoenetic.brokenpromises.survival.probe

import dev.zoenetic.brokenpromises.survival.CommonFixtures
import dev.zoenetic.brokenpromises.survival.CommonFixtures.climateScan
import dev.zoenetic.brokenpromises.survival.CommonFixtures.coldestSite
import dev.zoenetic.brokenpromises.survival.CommonFixtures.hottestSite
import net.minecraft.world.level.biome.Biomes
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RealClimateTests {

    @Test
    fun `the world contains somewhere genuinely hot`() {
        val hottest = hottestSite
        assertTrue(
            hottest.temperature.value > 25.0,
            "hottest column within ${CommonFixtures.SCAN_RADIUS_CHUNKS} chunks was " +
                    "${hottest.pos} at ${hottest.temperature.value}°C"
        )
    }

    @Test
    fun `the world contains somewhere genuinely cold`() {
        val coldest = coldestSite
        assertTrue(
            coldest.temperature.value < 5.0,
            "coldest column within ${CommonFixtures.SCAN_RADIUS_CHUNKS} chunks was " +
                    "${coldest.pos} at ${coldest.temperature.value}°C"
        )
    }

    @Test
    fun `hot and cold are far enough apart to be worth modelling`() {
        val spread = hottestSite.temperature.value - coldestSite.temperature.value
        assertTrue(spread > 20.0, "temperature spread across the scan was only $spread°C")
    }

    @Test
    fun `every scanned column stays inside the pole to equator band`() {
        val outside = climateScan.filter {
            it.temperature.value !in POLE_C..EQUATOR_C
        }
        assertTrue(outside.isEmpty(), "columns outside [$POLE_C, $EQUATOR_C]: ${outside.take(3)}")
    }

    @Test
    fun `a desert is not freezing`() {
        val desert = CommonFixtures.nearestSiteIn(
            Biomes.DESERT,
            Biomes.BADLANDS,
            Biomes.ERODED_BADLANDS,
            Biomes.WOODED_BADLANDS,
        )
        assertNotNull(desert, "no desert or badlands within the scan radius for this seed")
        assertTrue(
            desert.temperature.value > 20.0,
            "vanilla calls ${desert.pos} a desert; the model reads ${desert.temperature.value}°C"
        )
    }

    @Test
    fun `a snowy biome is not tropical`() {
        val snowy = CommonFixtures.nearestSiteIn(
            Biomes.SNOWY_PLAINS,
            Biomes.SNOWY_TAIGA,
            Biomes.ICE_SPIKES,
        )
        assertNotNull(snowy, "no snowy biome within the scan radius for this seed")
        assertTrue(
            snowy.temperature.value < 15.0,
            "vanilla calls ${snowy.pos} snowy; the model reads ${snowy.temperature.value}°C"
        )
    }

    @Test
    fun `vanilla's hot biomes read warmer than its snowy ones`() {
        val desert = CommonFixtures.nearestSiteIn(Biomes.DESERT, Biomes.BADLANDS)
        val snowy = CommonFixtures.nearestSiteIn(Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA)
        assertNotNull(desert)
        assertNotNull(snowy)
        assertTrue(
            desert.temperature.value > snowy.temperature.value,
            "desert ${desert.temperature.value}°C should beat snowy ${snowy.temperature.value}°C"
        )
    }

    @Test
    fun `the probe agrees with the scan for the same chunk`() {
        val chunk = CommonFixtures.climateChunk(hottestSite.pos)
        assertEquals(hottestSite.temperature.value, chunk.getBaseTemperature().value, 1e-9)
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            CommonFixtures.bootstrap()
        }
    }
}
