package io.wavebeans.daw

import io.wavebeans.lib.Sample
import io.wavebeans.lib.io.sine

class SineVoice : Voice() {
    override fun getSequence(
        frequency: Float,
        amplitude: Double,
        sampleOffset: Long,
        sampleRate: Float
    ): Sequence<Sample> {
        return frequency.sine(amplitude, timeOffset = sampleOffset.toDouble() / sampleRate.toDouble())
            .asSequence(sampleRate)
    }

}