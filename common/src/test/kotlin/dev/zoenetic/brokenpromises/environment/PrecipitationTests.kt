package dev.zoenetic.brokenpromises.environment

import kotlin.test.Test
import kotlin.test.assertTrue
import dev.zoenetic.brokenpromises.heat.Temperature

class PrecipitationTests {

    @Test
    fun `is monotonic in humidity at a fixed temperature`() {
        val t = Temperature(0.0)
        assertTrue(
            Precipitation.fromHumidityAndTemperature(-1.0, t).value <
            Precipitation.fromHumidityAndTemperature(-0.5, t).value)
        assertTrue(
            Precipitation.fromHumidityAndTemperature(-0.5, t).value <
            Precipitation.fromHumidityAndTemperature(0.0, t).value)
        assertTrue(
            Precipitation.fromHumidityAndTemperature(0.0, t).value <
                    Precipitation.fromHumidityAndTemperature(0.5, t).value)
        assertTrue(
            Precipitation.fromHumidityAndTemperature(0.5, t).value <
                    Precipitation.fromHumidityAndTemperature(1.0, t).value)
    }

    @Test
    fun `is monotonic in temperature at a fixed humidity`() {
        val h = 0.0
        assertTrue(
            Precipitation.fromHumidityAndTemperature(h, Temperature(-1.0)).value <
                    Precipitation.fromHumidityAndTemperature(h, Temperature(-0.5)).value)
        assertTrue(
            Precipitation.fromHumidityAndTemperature(h, Temperature(-0.5)).value <
                    Precipitation.fromHumidityAndTemperature(h, Temperature(0.0)).value)
        assertTrue(
            Precipitation.fromHumidityAndTemperature(h, Temperature(0.0)).value <
                    Precipitation.fromHumidityAndTemperature(h, Temperature(0.5)).value)
        assertTrue(
            Precipitation.fromHumidityAndTemperature(h, Temperature(0.5)).value <
                    Precipitation.fromHumidityAndTemperature(h, Temperature(1.0)).value)
    }

    @Test
    fun `is never negative`() {
        assertTrue(Precipitation.fromHumidityAndTemperature(-1e6, Temperature(-1e6)).value >= 0.0)
    }

    @Test
    fun `hot and dry is around 200MM per year`() {
        val hotAndDry = Precipitation.fromHumidityAndTemperature(-1.0, Temperature(30.0)).value
        val range = Pair(100, 300)
        assertTrue(hotAndDry > range.first, "expected $hotAndDry to be above ${range.first}")
        assertTrue(hotAndDry < range.second, "expected $hotAndDry to be below ${range.second}")
    }

    @Test
    fun `hot and wet is around 4000MM per year`() {
        val hotAndWet = Precipitation.fromHumidityAndTemperature(1.0, Temperature(30.0)).value
        val range = Pair(3000.0, 5000.0)
        assertTrue(hotAndWet > range.first, "expected $hotAndWet to be above ${range.first}")
        assertTrue(hotAndWet < range.second, "expected $hotAndWet to be below ${range.second}")
    }

    @Test
    fun `cold and wet is still dry`() {
        val coldAndWet = Precipitation.fromHumidityAndTemperature(1.0, Temperature(-10.0)).value
        val range = Pair(100.0, 300.0)
        assertTrue(coldAndWet > range.first, "expected $coldAndWet to be above ${range.first}")
        assertTrue(coldAndWet < range.second, "expected $coldAndWet to be below ${range.second}")
    }
}