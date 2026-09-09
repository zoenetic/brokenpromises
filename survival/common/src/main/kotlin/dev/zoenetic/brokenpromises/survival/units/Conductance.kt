package dev.zoenetic.brokenpromises.survival.units

@JvmInline
public value class Conductance(public val value: Double) {
    public operator fun plus(o: Conductance): Conductance = Conductance(value + o.value)
    public operator fun minus(o: Conductance): Conductance = Conductance(value - o.value)
    public operator fun times(k: Double): Conductance = Conductance(value * k)
    public operator fun times(o: Conductance): Conductance = Conductance(value * o.value)
    public operator fun div(o: Conductance): Conductance = Conductance(value / o.value)
    public operator fun unaryMinus(): Conductance = Conductance(-value)
    public operator fun compareTo(o: Conductance): Int = value.compareTo(o.value)

    public fun toDouble(): Double {
        return value
    }
}