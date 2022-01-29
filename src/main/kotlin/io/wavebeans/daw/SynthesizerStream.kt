package io.wavebeans.daw

import io.wavebeans.lib.AlterBean
import io.wavebeans.lib.BeanParams
import io.wavebeans.lib.BeanStream
import io.wavebeans.lib.Sample
import io.wavebeans.lib.SampleVector
import io.wavebeans.lib.ZeroSample

class SynthesizerStreamParams(
    val generator: Voice
) : BeanParams()

class SynthesizerStream(
    override val input: BeanStream<MidiChunk>,
    override val parameters: SynthesizerStreamParams
) : BeanStream<SampleVector>, AlterBean<MidiChunk, SampleVector> {

    override val desiredSampleRate: Float? = null

    override fun asSequence(sampleRate: Float): Sequence<SampleVector> {
        var noteOffset = 0L
        var activeGenerator: Iterator<Sample>? = null
        return input.asSequence(sampleRate)
            .map { buffer ->
                val result = SampleVector(buffer.length)
                val eventsByOffset = buffer.events.groupBy { it.offset }
                result.indices.forEach { idx ->
                    eventsByOffset[idx]?.forEach { e ->
                        when (e) {
                            is NoteOn -> {
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
                            is NoteOff -> {
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