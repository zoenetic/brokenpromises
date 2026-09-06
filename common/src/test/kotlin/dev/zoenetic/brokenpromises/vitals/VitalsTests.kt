package dev.zoenetic.brokenpromises.vitals

import dev.zoenetic.brokenpromises.heat.ConductiveMedium
import dev.zoenetic.brokenpromises.heat.ConductiveSurface
import dev.zoenetic.brokenpromises.environment.Wind
import net.minecraft.world.phys.Vec3
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class VitalsTests {

    @Test
    fun `approach returns current exactly for 0 elapsed ticks`() {
        val low = 30.0
        assertEquals(
            low,
            approach(low, low - 2.0, 0, BODY_COOLS_AT)
        )
        val high = 44.0
        assertEquals(
            high,
            approach(high, high + 2.0, 0, BODY_WARMS_AT)
        )

    }

    @Test
    fun `one half-life closes exactly half the gap`() {
        val low = 30.0
        val lower = low - 3.0
        assertEquals(
            low - ((low - (lower)) / 2),
            approach(low, low - 3.0, 20, 1.0)
        )
        val high = 44.0
        val higher = high + 3.0
        assertEquals(
            high + ((higher - high) / 2),
            approach(high, high + 3.0, 20, 1.0)
        )
    }

    @Test
    fun `approach never overshoots when warming`() {
        val current = NORMAL_BODY_TEMPERATURE
        for (gap in 1..10) {
            val target = current + gap
            for (elapsed in listOf(1L, 20L, 100_000L)) {
                val new = approach(
                    current,
                    target,
                    elapsed,
                    BODY_WARMS_AT
                )
                assertTrue(
                    new in current..target,
                    "gap $gap, elapsed $elapsed: got $new"
                )
            }
        }
    }

    @Test
    fun `approach never overshoots when cooling`() {
        val current = NORMAL_BODY_TEMPERATURE
        for (gap in 1..10) {
            val target = current - gap
            for (elapsed in listOf(1L, 20L, 100_000L)) {
                val new = approach(
                    current,
                    target,
                    elapsed,
                    BODY_COOLS_AT
                )
                assertTrue(
                    new in target..current,
                    "gap $gap, elapsed $elapsed: got $new"
                )
            }
        }
    }

    @Test
    fun `approach composes, 40 ticks is equal to 2 x 20 tickets`() {
        val modifier = 3.0
        val afterForty = approach(
            NORMAL_BODY_TEMPERATURE,
            NORMAL_BODY_TEMPERATURE + modifier,
            40,
            BODY_WARMS_AT
        )
        val afterFirstTwenty = approach(
            NORMAL_BODY_TEMPERATURE,
            NORMAL_BODY_TEMPERATURE + modifier,
            20,
            BODY_WARMS_AT
        )
        val afterSecondTwenty = approach(
            afterFirstTwenty,
            NORMAL_BODY_TEMPERATURE + modifier,
            20,
            BODY_WARMS_AT
        )
        assertEquals(afterForty, afterSecondTwenty, 1e-9)
    }

    @Test
    fun `if current == target, approach returns current`() {
        val current = 24.0
        assertEquals(
            current,
            approach(current, current, 1, BODY_WARMS_AT)
        )
        assertEquals(
            current,
            approach(current, current, 1, BODY_COOLS_AT)
        )
    }

    @Test
    fun `effective half life is the baseline when not in a conductive medium or on a conductive surface`() {
        assertEquals(
            BODY_WARMS_AT,
            effectiveHalfLife(
                isWarming = true,
            )
        )
        assertEquals(
            BODY_COOLS_AT,
            effectiveHalfLife(
                isWarming = false,
            )
        )
    }

    @Test
    fun `warming and cooling use different baselines`() {
        assertNotEquals(
            effectiveHalfLife(
                isWarming = true,
            ),
            effectiveHalfLife(
                isWarming = false,
            ),
        )
    }

    @Test
    fun `a conductive medium divides the baseline by its conductance`() {
        val water = ConductiveMedium.WATER.conductance
        assertEquals(
            BODY_COOLS_AT / water,
            effectiveHalfLife(
                isWarming = false,
                medium = water,
            )
        )
        assertEquals(
            BODY_WARMS_AT / water,
            effectiveHalfLife(
                isWarming = true,
                medium = water,
            )
        )
    }

    @Test
    fun `a conductive surface divides the baseline by its conductance`() {
        val metal = ConductiveSurface.METAL.conductance
        assertEquals(
            BODY_COOLS_AT / metal,
            effectiveHalfLife(
                isWarming = false,
                surface = metal
            )
        )
    }

    @Test
    fun `medium and surface conductance values multiply`() {
        val water = ConductiveMedium.WATER.conductance
        val metal = ConductiveSurface.METAL.conductance
        assertEquals(
            BODY_COOLS_AT / (water * metal),
            effectiveHalfLife(
                isWarming = false,
                medium = water,
                surface = metal
            ),
            1e-9,
        )
    }

    @Test
    fun `lava is effectively instantaneous`() {
        val lava = ConductiveMedium.LAVA.conductance
        assertTrue(
            effectiveHalfLife(
                isWarming = true,
                medium = lava,
            ) < 1.0
        )
    }

    @Test
    fun `target is normal anywhere inside the comfort band`() {
        for (ambient in listOf(COMFORT_LOW, 22.0, 25.0, 28.0, COMFORT_HIGH)) {
            assertEquals(NORMAL_BODY_TEMPERATURE, targetTemperature(ambient), "ambient $ambient")
        }
    }

    @Test
    fun `target is continuous at both band edges`() {
        val epsilon = 1e-6
        assertEquals(targetTemperature(COMFORT_LOW), targetTemperature(COMFORT_LOW - epsilon), 1e-3)
        assertEquals(
            targetTemperature(COMFORT_HIGH),
            targetTemperature(COMFORT_HIGH + epsilon),
            1e-3
        )
    }

    @Test
    fun `target moves with ambient outside the band, but by less than ambient does`() {
        val coldDrop = NORMAL_BODY_TEMPERATURE - targetTemperature(0.0)
        assertTrue(coldDrop > 0.0 && coldDrop < COMFORT_LOW - 0.0, "cold drop $coldDrop")
        val heatRise = targetTemperature(60.0) - NORMAL_BODY_TEMPERATURE
        assertTrue(heatRise > 0.0 && heatRise < 60.0 - COMFORT_HIGH, "heat rise $heatRise")
    }

    @Test
    fun `cold leaks through to the core more than heat does`() {
        val coldDrop = NORMAL_BODY_TEMPERATURE - targetTemperature(COMFORT_LOW - 10.0)
        val heatRise = targetTemperature(COMFORT_HIGH + 10.0) - NORMAL_BODY_TEMPERATURE
        assertTrue(coldDrop > heatRise)
    }

    @Test
    fun `calm wind has unit conductance and leaves the half life unchanged`() {
        assertEquals(1.0, Wind.CALM.conductance, 1e-9)
        assertEquals(BODY_COOLS_AT, effectiveHalfLife(isWarming = false, wind = Wind.CALM.conductance), 1e-9)
    }

    @Test
    fun `wind conductance grows linearly with speed`() {
        val fiveMetresPerSecond = Wind(Vec3(5.0, 0.0, 0.0))
        assertEquals(1.0 + Wind.CHILL * 5.0, fiveMetresPerSecond.conductance, 1e-9)
        assertEquals(BODY_COOLS_AT / (1.0 + Wind.CHILL * 5.0),
            effectiveHalfLife(isWarming = false, wind = fiveMetresPerSecond.conductance), 1e-9)
    }

    @Test
    fun `wind conductance depends on speed, not direction`() {
        val east = Wind(Vec3(3.0, 0.0, 0.0))
        val north = Wind(Vec3(0.0, 0.0, -3.0))
        val diagonal = Wind(Vec3(3.0 / Math.sqrt(2.0), 0.0, 3.0 / Math.sqrt(2.0)))
        assertEquals(east.conductance, north.conductance, 1e-9)
        assertEquals(east.conductance, diagonal.conductance, 1e-9)
    }

    @Test
    fun `wind multiplies with medium and surface`() {
        val water = ConductiveMedium.WATER.conductance
        val metal = ConductiveSurface.METAL.conductance
        val wind = Wind(Vec3(5.0, 0.0, 0.0)).conductance
        assertEquals(
            BODY_COOLS_AT / (water * metal * wind),
            effectiveHalfLife(isWarming = false, medium = water, surface = metal, wind = wind),
            1e-9,
        )
    }
}
