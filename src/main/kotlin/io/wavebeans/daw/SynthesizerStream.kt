package io.wavebeans.daw

import io.wavebeans.lib.AlterBean
import io.wavebeans.lib.BeanParams
import io.wavebeans.lib.BeanStream
import io.wavebeans.lib.Sample
import io.wavebeans.lib.SampleVector
import io.wavebeans.lib.ZeroSample
import io.wavebeans.lib.times
import kotlin.math.max
import kotlin.math.min

class SynthesizerStreamParams(
    val generator: Voice,
    val fadeInOut: Double
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
                                activeGenerator = parameters.generator.apply(
                                    Signal(
                                        e.frequency,
                                        1.0,
                                        noteOffset,
                                        sampleRate
                                    )
                                ).iterator()
                            }
                            is KeepNote -> {
                                activeGenerator = parameters.generator.apply(
                                    Signal(
                                        e.frequency,
                                        1.0,
                                        noteOffset,
                                        sampleRate
                                    )
                                ).iterator()
                            }
                            is NoteOff -> {
                                activeGenerator = null
                            }
                            else -> {
                                throw UnsupportedOperationException("MidiEvent $e is not supported")
                            }
                        }
                    }
                    result[idx] = activeGenerator?.next()?.also { noteOffset++ } ?: ZeroSample

                }

                if (parameters.fadeInOut > 0.0) {
                    val limit = max(buffer.length * 0.005, 2.0)

                    val fadeInOutVector = SampleVector(buffer.length) {
                        if (it < limit) {
                            min(
                                max(
                                    it / limit,
                                    0.0
                                ),
                                1.0
                            )
                        } else if (buffer.length - 1 - it <= limit) {
                            min(
                                max(
                                    (buffer.length - 1 - it) / limit,
                                    0.0
                                ),
                                1.0
                            )
                        } else {
                            1.0
                        }
                    }
                    result * fadeInOutVector
                } else {
                    result
                }
            }
    }
}