package dev.zoenetic.brokenpromises.effects.player

import dev.zoenetic.brokenpromises.vitals.NORMAL_BODY_TEMPERATURE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MovementsTests {

    @Test
    fun `temperatures in the dead zone give no speed reduction`() {
        val start =
            NORMAL_BODY_TEMPERATURE - SPEED_PENALTY_DEAD_ZONE
        val end =
            NORMAL_BODY_TEMPERATURE + SPEED_PENALTY_DEAD_ZONE
        for (temperature in start.toInt()..end.toInt()) {
            assertEquals(
                0.0,
                speedPenalty(temperature.toDouble())
            )
        }
    }

    @Test
    fun `temperatures just past each edge give small, positive reductions`() {
        val high =
            speedPenalty(NORMAL_BODY_TEMPERATURE + SPEED_PENALTY_DEAD_ZONE + 1)
        assertTrue(
            high > 0.0 && high < 1.0
        )
        val low =
            speedPenalty(NORMAL_BODY_TEMPERATURE - SPEED_PENALTY_DEAD_ZONE - 1)
        assertTrue(
            low > 0.0 && low < 1.0
        )
    }

    @Test
    fun `speed penalty is clamped at the maximum on both sides`() {
        for (temperature in listOf(HEAT_FULL_PENALTY_AT, 45.0, 60.0, 75.0)) {
            assertEquals(SPEED_PENALTY_MAX, speedPenalty(temperature), "hot: $temperature")
        }
        for (temperature in listOf(COLD_FULL_PENALTY_AT, 20.0, 10.0, 0.0)) {
            assertEquals(SPEED_PENALTY_MAX, speedPenalty(temperature), "cold: $temperature")
        }
    }

    @Test
    fun `speed penalties are monotonic`() {
        val lows = mutableListOf<Double>()
        for (temperature in 30..36) {
            val penalty =
                speedPenalty(temperature.toDouble())
            lows.add(penalty)
        }
        assertEquals(
            lows.sorted().reversed(),
            lows,
            "low temperature penalties should be sorted (in reverse)"
        )
        val highs = mutableListOf<Double>()
        for (temperature in 38..44) {
            val penalty =
                speedPenalty(temperature.toDouble())
            highs.add(penalty)
        }
        assertEquals(
            highs.sorted(),
            highs,
            "high temperature penalties should be sorted"
        )
    }

    @Test
    fun `heat penalises harder than cold at the same deviation`() {
        for (deviation in listOf(2.0, 3.0, 4.0)) {
            val hot = speedPenalty(NORMAL_BODY_TEMPERATURE + deviation)
            val cold = speedPenalty(NORMAL_BODY_TEMPERATURE - deviation)
            assertTrue(hot > cold, "deviation $deviation: hot $hot should exceed cold $cold")
        }
    }
}