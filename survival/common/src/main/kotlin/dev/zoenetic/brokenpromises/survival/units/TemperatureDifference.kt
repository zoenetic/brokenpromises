package dev.zoenetic.brokenpromises.survival.units

@JvmInline
public value class TemperatureDifference(public val value: Double) {
    public operator fun compareTo(o: TemperatureDifference): Int = value.compareTo(o.value)
}