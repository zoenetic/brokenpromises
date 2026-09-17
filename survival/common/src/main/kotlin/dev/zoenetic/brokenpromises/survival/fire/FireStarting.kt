package dev.zoenetic.brokenpromises.survival.fire

import kotlin.math.pow

public object FireStarting {

    internal const val K: Double = 4.0
    internal const val L: Double = 90.0

    public fun chance(attempt: Int): Double = (K / L) * (attempt / L).pow(K - 1)

}