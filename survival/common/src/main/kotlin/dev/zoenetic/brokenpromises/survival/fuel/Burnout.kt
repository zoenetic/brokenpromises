package dev.zoenetic.brokenpromises.survival.fuel

import dev.zoenetic.brokenpromises.survival.units.Duration
import dev.zoenetic.brokenpromises.survival.units.Time

@JvmInline
public value class Burnout(public val at: Time) {

    public fun fuelAt(now: Time, max: Fuel, perUnit: Duration): Fuel {
        val ticks = at - now
        val fuel =
            Math.ceilDiv(ticks.value, perUnit.value).coerceIn(0, max.level.toLong()).toInt()
        return Fuel(fuel)
    }

    public fun nextDropAt(now: Time, max: Fuel, perUnit: Duration): Time? {
        val fuel = fuelAt(now, max, perUnit)
        if (fuel.level == 0) return null
        return at - perUnit * (fuel.level - 1)
    }

    public fun isOut(now: Time): Boolean = now >= at

    public fun refuel(added: Fuel, now: Time, max: Fuel, perUnit: Duration): Burnout {
        val from = if (at > now) at else now
        val extended = from + perUnit * added.level
        val cap = now + perUnit * max.level
        return Burnout(if (extended < cap) extended else cap)
    }

    public companion object {

        public fun start(now: Time, fuel: Fuel, perUnit: Duration): Burnout {
            val ticksForFuel = perUnit * fuel.level
            return Burnout(now + ticksForFuel)
        }

    }

}