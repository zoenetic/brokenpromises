package dev.zoenetic.brokenpromises.environment

import dev.zoenetic.brokenpromises.heat.HeatSource
import dev.zoenetic.brokenpromises.heat.Power
import dev.zoenetic.brokenpromises.heat.sumHeatSources
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
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
}