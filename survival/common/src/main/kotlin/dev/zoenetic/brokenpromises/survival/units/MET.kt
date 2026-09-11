package dev.zoenetic.brokenpromises.survival.units

import java.util.*


@JvmInline
public value class MET(public val value: Double) {
    public fun toPower(): Power = Power(value * 100)
    public fun toCapacity(max: MET): Double = (value - 1) / (max.value - 1)
}

public fun ArrayDeque<MET>.average(): MET {
    var sum = 0.0
    var count = 0
    for (element in this) {
        sum += element.value
        if (++count < 0) {
            throw ArithmeticException("Count overflow has happened.")
        }
    }
    return if (count == 0) MET(1.0) else MET(sum / count)
}