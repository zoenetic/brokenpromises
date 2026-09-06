package dev.zoenetic.brokenpromises.environment

@JvmInline
public value class Humidity(public val value: Double) {
    public companion object {
        public fun fromNoise(n: Double): Humidity = Humidity((n.coerceIn(-1.0, 1.0) + 1.0) / 2.0)
    }
}
