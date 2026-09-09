package dev.zoenetic.brokenpromises.survival.state

import dev.zoenetic.brokenpromises.survival.units.Power
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeatSourcesTests {
    private val body = Vec3(0.5, 0.5, 0.5)

    private fun sources(vararg s: HeatSource) = mutableListOf(*s)

    @Test
    fun `no sources contribute nothing`() {
        assertEquals(0.0, sumHeatSources(body, mutableListOf()))
    }

    @Test
    fun `a more distant source contributes less than a nearer one`() {
        val near = sumHeatSources(body, sources(HeatSource(BlockPos(2, 0, 0), Power(25.0))))
        val far = sumHeatSources(body, sources(HeatSource(BlockPos(6, 0, 0), Power(25.0))))
        assertTrue(far < near, "expected $far to be less than $near")
    }

    @Test
    fun `two identical heat sources at equal distance give exactly double power`() {
        val power = Power(10.0)
        val a = HeatSource(BlockPos(3, 0, 0), power)
        val b = HeatSource(BlockPos(0, 0, 3), power)
        val one = sumHeatSources(body, sources(a))
        val two = sumHeatSources(body, sources(a, b))
        assertEquals(2 * one, two, 1e-9, "expected $two to be twice $one")
    }

    @Test
    fun `a source at zero distance contributes exactly power over the softening`() {
        val power = Power(10.0)
        val atBody = sources(HeatSource(BlockPos(0, 0, 0), power))
        assertEquals(power.value / HEAT_SOURCE_SOFTENING, sumHeatSources(body, atBody), 1e-9)
    }

    @Test
    fun `contribution keeps rising all the way in, with no plateau`() {
        val source = sources(HeatSource(BlockPos(0, 0, 0), Power(10.0)))
        val distances = listOf(2.0, 1.0, 0.5, 0.25, 0.1, 0.0)
        val heats = distances.map { d -> sumHeatSources(Vec3(0.5, 0.5, 0.5 + d), source) }
        for (i in 1 until heats.size) {
            assertTrue(
                heats[i] > heats[i - 1],
                "at ${distances[i]} (${heats[i]}) should exceed at ${distances[i - 1]} (${heats[i - 1]})"
            )
        }
    }

    @Test
    fun `far from the source the falloff is inverse square`() {
        val power = Power(10.0)
        val source = sources(HeatSource(BlockPos(0, 0, 0), power))
        val distance = 20.0
        val softened = sumHeatSources(Vec3(0.5, 0.5, 0.5 + distance), source)
        val inverseSquare = power.value / (distance * distance)
        val relativeError = abs(softened - inverseSquare) / inverseSquare
        assertTrue(
            relativeError < 0.005,
            "at $distance blocks softening should be negligible; relative error $relativeError"
        )
    }
}
