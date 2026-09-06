package dev.zoenetic.brokenpromises.heat

import dev.zoenetic.brokenpromises.environment.Humidity
import dev.zoenetic.brokenpromises.environment.Sky
import net.minecraft.SharedConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TemperatureTests {

    val dry = Humidity(0.0)
    val humid = Humidity(1.0)

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
    fun `the warmest tick is exactly the dry swing above the mean in dry air`() {
        val sky = Sky(1.0)
        assertEquals(
            mean.value + DIURNAL_SWING_DRY,
            mean.adjustForTimeOfDay(WARMEST_TICK, sky, dry).value,
            1e-9
        )
    }

    @Test
    fun `half a day after the warmest tick is exactly the dry swing below the mean in dry air`() {
        val sky = Sky(1.0)
        val coldest = WARMEST_TICK + dayTicks / 2
        assertEquals(
            mean.value - DIURNAL_SWING_DRY,
            mean.adjustForTimeOfDay(coldest, sky, dry).value,
            1e-9
        )
    }

    @Test
    fun `time of day adjustment repeats every game day`() {
        val sky = Sky(0.5)
        for (time in listOf(0L, 3000L, WARMEST_TICK, 17500L, 23999L)) {
            val today = mean.adjustForTimeOfDay(time, sky, dry).value
            val tomorrow = mean.adjustForTimeOfDay(time + dayTicks, sky, dry).value
            val nextWeek = mean.adjustForTimeOfDay(time + 7 * dayTicks, sky, dry).value
            assertEquals(today, tomorrow, 1e-9, "tick $time")
            assertEquals(today, nextWeek, 1e-9, "tick $time")
        }
    }

    @Test
    fun `evenly spaced samples across a day average to the mean`() {
        val sky = Sky(0.5)
        val samples = 24
        val average = (0 until samples)
            .map { mean.adjustForTimeOfDay(it * dayTicks / samples, sky, dry).value }
            .average()
        assertEquals(mean.value, average, 1e-9)
    }

    @Test
    fun `the coldest moment is before dawn, not midnight`() {
        val sky = Sky(0.5)
        val sunrise = mean.adjustForTimeOfDay(0L, sky, dry).value
        val noon = mean.adjustForTimeOfDay(6000L, sky, dry).value
        val midnight = mean.adjustForTimeOfDay(18000L, sky, dry).value
        val beforeDawn = mean.adjustForTimeOfDay(21000L, sky, dry).value
        assertTrue(
            beforeDawn < midnight,
            "before dawn $beforeDawn should be colder than midnight $midnight"
        )
        assertTrue(
            beforeDawn < sunrise,
            "before dawn $beforeDawn should be colder than sunrise $sunrise"
        )
        assertTrue(sunrise < noon, "sunrise $sunrise should be colder than noon $noon")
    }

    @Test
    fun `sky openness scales the swing`() {
        for (sky in listOf(0.25, 0.5, 0.75)) {
            assertEquals(
                mean.value + DIURNAL_SWING_DRY * sky,
                mean.adjustForTimeOfDay(WARMEST_TICK, Sky(sky), dry).value,
                1e-9,
                "sky $sky"
            )
        }
    }

    @Test
    fun `with no sky there is no diurnal swing`() {
        for (time in listOf(0L, 6000L, WARMEST_TICK, 18000L, 21000L)) {
            assertEquals(
                mean.value,
                mean.adjustForTimeOfDay(time, Sky(0.0), dry).value,
                1e-9,
                "tick $time"
            )
        }
    }

    @Test
    fun `dry air gives exactly the dry swing and humid air exactly the humid swing`() {
        val sky = Sky(1.0)
        assertEquals(
            mean.value + DIURNAL_SWING_DRY,
            mean.adjustForTimeOfDay(WARMEST_TICK, sky, dry).value,
            1e-9
        )
        assertEquals(
            mean.value + DIURNAL_SWING_HUMID,
            mean.adjustForTimeOfDay(WARMEST_TICK, sky, humid).value,
            1e-9
        )
    }

    @Test
    fun `the swing shrinks as humidity rises`() {
        val sky = Sky(1.0)
        val swings = listOf(0.0, 0.25, 0.5, 0.75, 1.0)
            .map { h -> mean.adjustForTimeOfDay(WARMEST_TICK, sky, Humidity(h)).value - mean.value }
        for (i in 1 until swings.size) {
            assertTrue(
                swings[i] < swings[i - 1],
                "swing at humidity step $i (${swings[i]}) should be below step ${i - 1} (${swings[i - 1]})"
            )
        }
    }

    @Test
    fun `humidity narrows the swing but does not move the mean`() {
        val sky = Sky(1.0)
        for (h in listOf(0.0, 0.5, 1.0)) {
            val samples = 24
            val average = (0 until samples)
                .map { mean.adjustForTimeOfDay(it * dayTicks / samples, sky, Humidity(h)).value }
                .average()
            assertEquals(mean.value, average, 1e-9, "humidity $h")
        }
    }
}
