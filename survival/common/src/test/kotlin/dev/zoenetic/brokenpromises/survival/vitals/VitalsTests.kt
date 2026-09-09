package dev.zoenetic.brokenpromises.survival.vitals

import dev.zoenetic.brokenpromises.survival.probe.ConductiveMedium
import dev.zoenetic.brokenpromises.survival.probe.ConductiveSurface
import dev.zoenetic.brokenpromises.survival.units.CALM
import dev.zoenetic.brokenpromises.survival.units.CHILL
import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.units.Ticks
import dev.zoenetic.brokenpromises.survival.units.Wind
import net.minecraft.world.phys.Vec3
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class VitalsTests {

    private fun c(v: Double) = Celsius(v)
    private fun t(v: Long) = Ticks(v)

    @Test
    fun `approach returns current exactly for 0 elapsed ticks`() {
        val low = c(30.0)
        assertEquals(low, approach(low, c(28.0), t(0), BODY_COOLS_AT))
        val high = c(44.0)
        assertEquals(high, approach(high, c(46.0), t(0), BODY_WARMS_AT))
    }

    @Test
    fun `one half-life closes exactly half the gap`() {
        val oneSecond = dev.zoenetic.brokenpromises.survival.units.Conductance(1.0)
        assertEquals(28.5, approach(c(30.0), c(27.0), t(20), oneSecond).value, 1e-9)
        assertEquals(45.5, approach(c(44.0), c(47.0), t(20), oneSecond).value, 1e-9)
    }

    @Test
    fun `approach never overshoots when warming`() {
        val current = NORMAL_BODY_TEMPERATURE
        for (gap in 1..10) {
            val target = current + c(gap.toDouble())
            for (elapsed in listOf(1L, 20L, 100_000L)) {
                val new = approach(current, target, t(elapsed), BODY_WARMS_AT)
                assertTrue(
                    new >= current && new <= target,
                    "gap $gap, elapsed $elapsed: got ${new.value}"
                )
            }
        }
    }

    @Test
    fun `approach never overshoots when cooling`() {
        val current = NORMAL_BODY_TEMPERATURE
        for (gap in 1..10) {
            val target = current - c(gap.toDouble())
            for (elapsed in listOf(1L, 20L, 100_000L)) {
                val new = approach(current, target, t(elapsed), BODY_COOLS_AT)
                assertTrue(
                    new >= target && new <= current,
                    "gap $gap, elapsed $elapsed: got ${new.value}"
                )
            }
        }
    }

    @Test
    fun `approach composes, 40 ticks equals 2 x 20 ticks`() {
        val target = NORMAL_BODY_TEMPERATURE + c(3.0)
        val afterForty = approach(NORMAL_BODY_TEMPERATURE, target, t(40), BODY_WARMS_AT)
        val afterFirstTwenty = approach(NORMAL_BODY_TEMPERATURE, target, t(20), BODY_WARMS_AT)
        val afterSecondTwenty = approach(afterFirstTwenty, target, t(20), BODY_WARMS_AT)
        assertEquals(afterForty.value, afterSecondTwenty.value, 1e-9)
    }

    @Test
    fun `one tick at a time equals one step of twenty ticks`() {
        val target = NORMAL_BODY_TEMPERATURE - c(8.0)
        var stepwise = NORMAL_BODY_TEMPERATURE
        repeat(20) { stepwise = approach(stepwise, target, t(1), BODY_COOLS_AT) }
        val oneGo = approach(NORMAL_BODY_TEMPERATURE, target, t(20), BODY_COOLS_AT)
        assertEquals(oneGo.value, stepwise.value, 1e-9)
    }

    @Test
    fun `a single tick actually moves the body temperature`() {
        val target = NORMAL_BODY_TEMPERATURE - c(20.0)
        val after = approach(NORMAL_BODY_TEMPERATURE, target, t(1), BODY_COOLS_AT)
        assertTrue(
            after.value < NORMAL_BODY_TEMPERATURE.value,
            "one tick in the cold should cool the body, got ${after.value}"
        )
    }

    @Test
    fun `if current == target, approach returns current`() {
        val current = c(24.0)
        assertEquals(current, approach(current, current, t(1), BODY_WARMS_AT))
        assertEquals(current, approach(current, current, t(1), BODY_COOLS_AT))
    }

    @Test
    fun `effective half life is the baseline with no medium, surface or wind`() {
        assertEquals(BODY_WARMS_AT, effectiveHalfLife(isWarming = true))
        assertEquals(BODY_COOLS_AT, effectiveHalfLife(isWarming = false))
    }

    @Test
    fun `warming and cooling use different baselines`() {
        assertNotEquals(effectiveHalfLife(isWarming = true), effectiveHalfLife(isWarming = false))
    }

    @Test
    fun `a conductive medium divides the baseline by its conductance`() {
        val water = ConductiveMedium.WATER.conductance
        assertEquals(BODY_COOLS_AT / water, effectiveHalfLife(isWarming = false, medium = water))
        assertEquals(BODY_WARMS_AT / water, effectiveHalfLife(isWarming = true, medium = water))
    }

    @Test
    fun `a conductive surface divides the baseline by its conductance`() {
        val metal = ConductiveSurface.METAL.conductance
        assertEquals(BODY_COOLS_AT / metal, effectiveHalfLife(isWarming = false, surface = metal))
    }

    @Test
    fun `medium and surface conductance values multiply`() {
        val water = ConductiveMedium.WATER.conductance
        val metal = ConductiveSurface.METAL.conductance
        assertEquals(
            (BODY_COOLS_AT / (water * metal)).value,
            effectiveHalfLife(isWarming = false, medium = water, surface = metal).value,
            1e-9,
        )
    }

    @Test
    fun `lava is effectively instantaneous`() {
        val lava = ConductiveMedium.LAVA.conductance
        assertTrue(effectiveHalfLife(isWarming = true, medium = lava).value < 1.0)
    }

    @Test
    fun `target is normal anywhere inside the comfort band`() {
        for (ambient in listOf(COMFORT_LOW, c(22.0), c(25.0), c(28.0), COMFORT_HIGH)) {
            assertEquals(NORMAL_BODY_TEMPERATURE, targetTemperature(ambient), "ambient $ambient")
        }
    }

    @Test
    fun `target is continuous at both band edges`() {
        val epsilon = c(1e-6)
        assertEquals(
            targetTemperature(COMFORT_LOW).value,
            targetTemperature(COMFORT_LOW - epsilon).value,
            1e-3
        )
        assertEquals(
            targetTemperature(COMFORT_HIGH).value,
            targetTemperature(COMFORT_HIGH + epsilon).value,
            1e-3
        )
    }

    @Test
    fun `target moves with ambient outside the band, but by less than ambient does`() {
        val coldDrop = (NORMAL_BODY_TEMPERATURE - targetTemperature(c(0.0))).value
        assertTrue(coldDrop > 0.0 && coldDrop < COMFORT_LOW.value, "cold drop $coldDrop")
        val heatRise = (targetTemperature(c(60.0)) - NORMAL_BODY_TEMPERATURE).value
        assertTrue(heatRise > 0.0 && heatRise < 60.0 - COMFORT_HIGH.value, "heat rise $heatRise")
    }

    @Test
    fun `cold leaks through to the core more than heat does`() {
        val coldDrop = (NORMAL_BODY_TEMPERATURE - targetTemperature(COMFORT_LOW - c(10.0))).value
        val heatRise = (targetTemperature(COMFORT_HIGH + c(10.0)) - NORMAL_BODY_TEMPERATURE).value
        assertTrue(coldDrop > heatRise)
    }

    @Test
    fun `calm wind has unit conductance and leaves the half life unchanged`() {
        assertEquals(1.0, CALM.conductance.value, 1e-9)
        assertEquals(
            BODY_COOLS_AT.value,
            effectiveHalfLife(isWarming = false, wind = CALM.conductance).value,
            1e-9
        )
    }

    @Test
    fun `wind conductance grows linearly with speed`() {
        val fiveMetresPerSecond = Wind(Vec3(5.0, 0.0, 0.0))
        assertEquals(1.0 + CHILL * 5.0, fiveMetresPerSecond.conductance.value, 1e-9)
        assertEquals(
            (BODY_COOLS_AT.value) / (1.0 + CHILL * 5.0),
            effectiveHalfLife(isWarming = false, wind = fiveMetresPerSecond.conductance).value,
            1e-9
        )
    }

    @Test
    fun `wind conductance depends on speed, not direction`() {
        val east = Wind(Vec3(3.0, 0.0, 0.0))
        val north = Wind(Vec3(0.0, 0.0, -3.0))
        val diagonal = Wind(Vec3(3.0 / Math.sqrt(2.0), 0.0, 3.0 / Math.sqrt(2.0)))
        assertEquals(east.conductance.value, north.conductance.value, 1e-9)
        assertEquals(east.conductance.value, diagonal.conductance.value, 1e-9)
    }

    @Test
    fun `wind multiplies with medium and surface`() {
        val water = ConductiveMedium.WATER.conductance
        val metal = ConductiveSurface.METAL.conductance
        val wind = Wind(Vec3(5.0, 0.0, 0.0)).conductance
        assertEquals(
            (BODY_COOLS_AT / (water * metal * wind)).value,
            effectiveHalfLife(isWarming = false, medium = water, surface = metal, wind = wind).value,
            1e-9,
        )
    }
}
