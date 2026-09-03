package dev.zoenetic.brokenpromises.heat

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
        assertTrue(temperatureAtZeroNoise > range.first, "expected $temperatureAtZeroNoise to be greater than ${range.first}" )
        assertTrue(temperatureAtZeroNoise < range.second, "expected $temperatureAtZeroNoise to be less than ${range.second}" )
    }

    @Test
    fun `adjusting for altitude has no effect at or below sea level`() {
        val t = Temperature(0.0)
        val atSeaLevel = adjustTemperatureForAltitude(t, 0).value
        assertEquals(0.0, atSeaLevel, "expected 0, got $atSeaLevel")
        val belowSeaLevel = adjustTemperatureForAltitude(t, -100).value
        assertEquals(0.0, belowSeaLevel, "expected 0, got $belowSeaLevel")
    }

    @Test
    fun `adjusting for altitude is monotonic above sea level`() {
        val t = Temperature(0.0)
        val top = adjustTemperatureForAltitude(t, 300).value
        val middle = adjustTemperatureForAltitude(t, 150).value
        val bottom = adjustTemperatureForAltitude(t, 1).value
        assertTrue(top < middle, "expected $top to be less than $middle")
        assertTrue(middle < bottom, "expected $middle to be less than $bottom")
    }

    @Test
    fun `a big spike in altitude gives a big drop in temperature`() {
        val t = Temperature(0.0)
        val altitude = 300
        val range = Pair(-30.0, -10.0)
        val adjusted = adjustTemperatureForAltitude(t, altitude).value
        assertTrue(adjusted > range.first, "expected $adjusted to be greater than ${range.first}")
        assertTrue(adjusted < range.second, "expected $adjusted to be less than ${range.second}")
    }
}