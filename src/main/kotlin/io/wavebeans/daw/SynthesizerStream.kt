package io.wavebeans.daw

import io.wavebeans.lib.AnyBean
import io.wavebeans.lib.BeanParams
import io.wavebeans.lib.BeanStream
import io.wavebeans.lib.Fn
import io.wavebeans.lib.Sample
import io.wavebeans.lib.SampleVector
import io.wavebeans.lib.ZeroSample

data class Signal(
    val frequency: Float,
    val amplitude: Double,
    val sampleOffset: Long
)

class SynthesizerStreamParams(
    val generator: Fn<Signal, Sequence<Sample>>
) : BeanParams()

class SynthesizerStream(
    private val input: BeanStream<MidiBuffer>,
    override val parameters: SynthesizerStreamParams
) : BeanStream<SampleVector> {

    override fun inputs(): List<AnyBean> = listOf(input)

    override val desiredSampleRate: Float? = null

    override fun asSequence(sampleRate: Float): Sequence<SampleVector> {
        var noteOffset = 0L
        var activeGenerator: Iterator<Sample>? = null
        return input.asSequence(sampleRate)
            .asSequence()
            .map { buffer ->
                val result = SampleVector(buffer.length)
                val eventsByOffset = buffer.events.groupBy { it.offset }
                result.indices.forEach { idx ->
                    eventsByOffset[idx]?.forEach { e ->
                        when (e) {
                            is StartNote -> {
                                noteOffset = 0
                                activeGenerator = parameters.generator.apply(
                                    Signal(
                                        e.frequency,
                                        1.0,
                                        0L
                                    )
                                ).iterator()
                            }
                            is KeepNote -> {
                                activeGenerator = parameters.generator.apply(
                                    Signal(
                                        e.frequency,
                                        1.0,
                                        noteOffset
                                    )
                                ).iterator()
                            }
                            is EndNote -> {
                                activeGenerator = null
                                noteOffset = 0
                            }
                            else -> {
                                throw UnsupportedOperationException("MidiEvent $e is not supported")
                            }
                        }
                    }
                    result[idx] = activeGenerator?.next()?.also { noteOffset++ } ?: ZeroSample

                }
                result
            }
    }
}