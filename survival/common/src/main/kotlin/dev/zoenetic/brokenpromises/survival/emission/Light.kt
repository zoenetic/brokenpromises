package dev.zoenetic.brokenpromises.survival.emission

@JvmInline
public value class Light(public val value: Int) {
    init {
        require(value in 0..15) { "Light value out of range: $value" }
    }

    // light doesn't add up: the brightest source wins
    public operator fun compareTo(o: Light): Int = value.compareTo(o.value)

    public companion object {
        public val NONE: Light = Light(0)
        public val MAX: Light = Light(15)
    }
}
