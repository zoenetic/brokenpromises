package dev.zoenetic.brokenpromises.survival.emission

public data class Light(public val value: Int) {
    init {
        require(value in 0..15) { "Light value out of range: $value" }
    }

    public operator fun compareTo(o: Light): Int = value.compareTo(o.value)
    public operator fun times(i: Int): Light = Light(value * i)

    public companion object {
        public val NONE: Light = Light(0)
        public val MAX: Light = Light(15)
    }
}
