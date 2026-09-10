package dev.zoenetic.brokenpromises.survival.effects.player

import dev.zoenetic.brokenpromises.survival.units.Celsius
import dev.zoenetic.brokenpromises.survival.vitals.NORMAL_BODY_TEMPERATURE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MovementsTests {

    private fun penalty(temperature: Double): Double =
        SpeedPenalty.forTemperature(Celsius(temperature)).value

    private val normal = NORMAL_BODY_TEMPERATURE

    @Test
    fun `temperatures in the dead zone give no speed reduction`() {
        val start = normal - SPEED_PENALTY_DEAD_ZONE
        val end = normal + SPEED_PENALTY_DEAD_ZONE
        for (temperature in start.toInt()..end.toInt()) {
            assertEquals(0.0, penalty(temperature.toDouble()), "at $temperature")
        }
    }

    @Test
    fun `temperatures just past each edge give small, positive reductions`() {
        val high = penalty(normal + SPEED_PENALTY_DEAD_ZONE + 1)
        assertTrue(high > 0.0 && high < 1.0, "high $high")
        val low = penalty(normal - SPEED_PENALTY_DEAD_ZONE - 1)
        assertTrue(low > 0.0 && low < 1.0, "low $low")
    }

    @Test
    fun `speed penalty is clamped at the maximum on both sides`() {
        for (temperature in listOf(HEAT_FULL_PENALTY_AT, 45.0, 60.0, 75.0)) {
            assertEquals(SPEED_PENALTY_MAX.value, penalty(temperature), "hot: $temperature")
        }
        for (temperature in listOf(COLD_FULL_PENALTY_AT, 20.0, 10.0, 0.0)) {
            assertEquals(SPEED_PENALTY_MAX.value, penalty(temperature), "cold: $temperature")
        }
    }

    @Test
    fun `speed penalties are monotonic`() {
        val lows = (30..36).map { penalty(it.toDouble()) }
        assertEquals(
            lows.sorted().reversed(),
            lows,
            "low temperature penalties should be sorted (in reverse)"
        )
        val highs = (38..44).map { penalty(it.toDouble()) }
        assertEquals(highs.sorted(), highs, "high temperature penalties should be sorted")
    }

    @Test
    fun `heat penalises harder than cold at the same deviation`() {
        for (deviation in listOf(2.0, 3.0, 4.0)) {
            val hot = penalty(normal + deviation)
            val cold = penalty(normal - deviation)
            assertTrue(hot > cold, "deviation $deviation: hot $hot should exceed cold $cold")
        }
    }
}
