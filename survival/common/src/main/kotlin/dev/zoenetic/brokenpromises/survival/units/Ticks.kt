package dev.zoenetic.brokenpromises.survival.units

@JvmInline
public value class Ticks(public val value: Long) {
    public operator fun plus(o: Ticks): Ticks = Ticks(value + o.value)
    public operator fun minus(o: Ticks): Ticks = Ticks(value - o.value)
    public operator fun times(k: Int): Ticks = Ticks(value * k)
    public operator fun div(o: Ticks): Ticks = Ticks(value / o.value)
    public operator fun unaryMinus(): Ticks = Ticks(-value)
    public operator fun compareTo(o: Ticks): Int = value.compareTo(o.value)
}