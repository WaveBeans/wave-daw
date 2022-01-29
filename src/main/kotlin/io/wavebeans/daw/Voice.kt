package io.wavebeans.daw

import io.wavebeans.lib.Fn
import io.wavebeans.lib.Sample

typealias VoiceKey = String

data class Signal(
    val frequency: Float,
    val amplitude: Double,
    val sampleOffset: Long
)

abstract class Voice : Fn<Signal, Sequence<Sample>>() {

    override fun apply(argument: Signal): Sequence<Sample> {
        return getSequence(argument.frequency, argument.amplitude, argument.sampleOffset)
    }

    abstract fun getSequence(frequency: Float, amplitude: Double, sampleOffset: Long): Sequence<Sample>

}