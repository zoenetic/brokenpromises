package dev.zoenetic.brokenpromises.environment

import dev.zoenetic.brokenpromises.heat.HeatSource
import dev.zoenetic.brokenpromises.heat.MIN_HEAT_DISTANCE_SQ
import dev.zoenetic.brokenpromises.heat.Power
import dev.zoenetic.brokenpromises.heat.sumHeatSources
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConditionsTests {
    private val body = Vec3(0.5, 0.5, 0.5)

    @Test
    fun `no sources contribute nothing`() {
        assertEquals(0.0, sumHeatSources(body, emptyList()))
    }

    @Test
    fun `a more distant source contributes less than a nearer one`() {
        val near = sumHeatSources(
            body,
            listOf(
                HeatSource(
                    BlockPos(2, 0, 0),
                    Power(25.0)
                )
            )
        )
        val far = sumHeatSources(
            body,
            listOf(
                HeatSource(
                    BlockPos(6, 0, 0),
                    Power(25.0)
                )
            )
        )
        assertTrue(
            far < near,
            "expected $far to be less than $near"
        )
    }

    @Test
    fun `two identical heat sources at equal distance (above the clamp distance) give exactly double power`() {
        val power = Power(10.0)
        val a = HeatSource(BlockPos(3, 0, 0), power)
        val b = HeatSource(BlockPos(0, 0, 3), power)
        val one = sumHeatSources(body, listOf(a))
        val two = sumHeatSources(body, listOf(a, b))
        assertEquals(
            2 * one,
            two,
            1e-9,
            "expected $two to be twice $one"
        )
    }

    @Test
    fun `a source closer than the clamp distance contributes as if at the clamp distance`() {
        val power = Power(10.0)
        val expected =
            2 * power.value / MIN_HEAT_DISTANCE_SQ
        val a = HeatSource(BlockPos(0, 0, 0), power)
        val b = HeatSource(BlockPos(0, 0, 0), power)
        assertEquals(
            expected,
            sumHeatSources(body, listOf(a, b)),
            1e-9
        )
    }

    @Test
    fun `at the clamp distance and inside it give the same contribution`() {
        val power = Power(10.0)
        val source =
            listOf(HeatSource(BlockPos(0, 0, 0), power))
        val clampDistance = sqrt(MIN_HEAT_DISTANCE_SQ)
        val atClamp = sumHeatSources(
            Vec3(
                0.5,
                0.5,
                0.5 + clampDistance
            ), source
        )
        val insideClamp = sumHeatSources(
            Vec3(
                0.5,
                0.5,
                0.5 + clampDistance / 2
            ), source
        )
        assertEquals(atClamp, insideClamp, 1e-9)
        assertEquals(
            power.value / MIN_HEAT_DISTANCE_SQ,
            atClamp,
            1e-9
        )
    }
}
