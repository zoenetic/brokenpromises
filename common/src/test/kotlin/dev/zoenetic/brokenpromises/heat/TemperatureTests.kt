package dev.zoenetic.brokenpromises.heat

import net.minecraft.SharedConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TemperatureTests {

    @Test
    fun `temperature from noise is monotonic`() {
        assertTrue(Temperature.fromNoise(-1.0).value < Temperature.fromNoise(-0.5).value)
        assertTrue(Temperature.fromNoise(-0.5).value < Temperature.fromNoise(0.0).value)
        assertTrue(Temperature.fromNoise(0.0).value < Temperature.fromNoise(0.5).value)
        assertTrue(Temperature.fromNoise(0.5).value < Temperature.fromNoise(1.0).value)
    }

    @Test
    fun `values are clamped at pole and equator`() {
        assertEquals(Temperature.fromNoise(-1.0).value, Temperature.fromNoise(-2.0).value)
        assertEquals(Temperature.fromNoise(-2.0).value, Temperature.fromNoise(-1e6).value)
        assertEquals(Temperature.fromNoise(1.0).value, Temperature.fromNoise(2.0).value)
        assertEquals(Temperature.fromNoise(2.0).value, Temperature.fromNoise(1e6).value)
    }

    @Test
    fun `noise 0 is around 45 degree latitude`() {
        val temperatureAtZeroNoise = Temperature.fromNoise(0.0).value
        val range = Pair(10, 14)
        assertTrue(
            temperatureAtZeroNoise > range.first,
            "expected $temperatureAtZeroNoise to be greater than ${range.first}"
        )
        assertTrue(
            temperatureAtZeroNoise < range.second,
            "expected $temperatureAtZeroNoise to be less than ${range.second}"
        )
    }

    @Test
    fun `adjusting for altitude has no effect at or below sea level`() {
        val t = Temperature(0.0)
        val atSeaLevel = t.adjustForAltitude(0).value
        assertEquals(0.0, atSeaLevel, "expected 0, got $atSeaLevel")
        val belowSeaLevel = t.adjustForAltitude(-100).value
        assertEquals(0.0, belowSeaLevel, "expected 0, got $belowSeaLevel")
    }

    @Test
    fun `adjusting for altitude is monotonic above sea level`() {
        val t = Temperature(0.0)
        val top = t.adjustForAltitude(300).value
        val middle = t.adjustForAltitude(150).value
        val bottom = t.adjustForAltitude(1).value
        assertTrue(top < middle, "expected $top to be less than $middle")
        assertTrue(middle < bottom, "expected $middle to be less than $bottom")
    }

    @Test
    fun `a big spike in altitude gives a big drop in temperature`() {
        val t = Temperature(0.0)
        val altitude = 300
        val range = Pair(-30.0, -10.0)
        val adjusted = t.adjustForAltitude(altitude).value
        assertTrue(adjusted > range.first, "expected $adjusted to be greater than ${range.first}")
        assertTrue(adjusted < range.second, "expected $adjusted to be less than ${range.second}")
    }

    private val dayTicks = SharedConstants.TICKS_PER_GAME_DAY.toLong()
    private val mean = Temperature(10.0)

    @Test
    fun `the warmest tick is exactly the swing above the mean`() {
        assertEquals(mean.value + DIURNAL_SWING, mean.adjustForTimeOfDay(WARMEST_TICK).value, 1e-9)
    }

    @Test
    fun `half a day after the warmest tick is exactly the swing below the mean`() {
        val coldest = WARMEST_TICK + dayTicks / 2
        assertEquals(mean.value - DIURNAL_SWING, mean.adjustForTimeOfDay(coldest).value, 1e-9)
    }

    @Test
    fun `time of day adjustment repeats every game day`() {
        for (time in listOf(0L, 3000L, WARMEST_TICK, 17500L, 23999L)) {
            val today = mean.adjustForTimeOfDay(time).value
            val tomorrow = mean.adjustForTimeOfDay(time + dayTicks).value
            val nextWeek = mean.adjustForTimeOfDay(time + 7 * dayTicks).value
            assertEquals(today, tomorrow, 1e-9, "tick $time")
            assertEquals(today, nextWeek, 1e-9, "tick $time")
        }
    }

    @Test
    fun `evenly spaced samples across a day average to the mean`() {
        val samples = 24
        val average = (0 until samples)
            .map { mean.adjustForTimeOfDay(it * dayTicks / samples).value }
            .average()
        assertEquals(mean.value, average, 1e-9)
    }

    @Test
    fun `the coldest moment is before dawn, not midnight`() {
        val sunrise = mean.adjustForTimeOfDay(0L).value
        val noon = mean.adjustForTimeOfDay(6000L).value
        val midnight = mean.adjustForTimeOfDay(18000L).value
        val beforeDawn = mean.adjustForTimeOfDay(21000L).value
        assertTrue(beforeDawn < midnight, "before dawn $beforeDawn should be colder than midnight $midnight")
        assertTrue(beforeDawn < sunrise, "before dawn $beforeDawn should be colder than sunrise $sunrise")
        assertTrue(sunrise < noon, "sunrise $sunrise should be colder than noon $noon")
    }
}
