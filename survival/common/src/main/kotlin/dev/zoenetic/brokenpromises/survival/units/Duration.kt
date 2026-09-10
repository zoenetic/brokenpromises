package dev.zoenetic.brokenpromises.survival.units

@JvmInline
public value class Duration(public val value: Long) {
    public operator fun plus(o: Duration): Duration = Duration(this.value + o.value)
    public operator fun minus(o: Duration): Duration = Duration(this.value - o.value)
    public operator fun times(k: Double): Duration = Duration(this.value * k.toLong())
    public operator fun compareTo(o: Duration): Int = value.compareTo(o.value)
}