package dev.zoenetic.brokenpromises.survival.units

@JvmInline
public value class TemperatureDifference(public val value: Double) {
    public operator fun plus(o: TemperatureDifference): TemperatureDifference =
        TemperatureDifference(value + o.value)

    public operator fun compareTo(o: TemperatureDifference): Int = value.compareTo(o.value)
}
