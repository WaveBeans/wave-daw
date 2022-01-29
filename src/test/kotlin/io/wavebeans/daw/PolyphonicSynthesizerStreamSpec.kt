package io.wavebeans.daw

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.index
import assertk.assertions.isCloseTo
import assertk.assertions.isEqualTo
import assertk.assertions.size
import io.wavebeans.lib.AnyBean
import io.wavebeans.lib.BeanParams
import io.wavebeans.lib.BeanStream
import io.wavebeans.lib.Sample
import io.wavebeans.lib.ZeroSample
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class PolyphonicSynthesizerStreamSpec : Spek({

    describe("2 voice synth") {

        it("should interpret note sequence correctly") {
            val stream = synthesizerStream(
                listOf(
                    PolyphonicMidiChunk(
                        mapOf(
                            "1" to listOf(NoteOn(100.0f, 0)),
                            "2" to listOf(NoteOn(150.0f, 0))
                        ),
                        6
                    ),
                    PolyphonicMidiChunk(
                        mapOf(
                            "1" to listOf(NoteOff(0)),
                            "2" to listOf(NoteOff(0))
                        ),
                        2
                    ),
                    PolyphonicMidiChunk(
                        mapOf(
                            "1" to listOf(NoteOn(300.0f, 0)),
                        ),
                        2
                    ),
                    PolyphonicMidiChunk(
                        mapOf(
                            "1" to listOf(KeepNote(300.0f, 0)),
                            "2" to listOf(NoteOn(350.0f, 1)),
                        ),
                        2
                    ),
                )
            ).asSequence(1000.0f).toList()

            assertThat(stream).all {
                size().isEqualTo(4)
                index(0).all {
                    index(0).isCloseTo(
                        1_000_000.0 + 100.0 + 0.000 +
                                2_000_000.0 + 150.0 + 0.000,
                        1e-4
                    )
                    index(1).isCloseTo(
                        1_000_000.0 + 100.0 + 0.001 +
                                2_000_000.0 + 150.0 + 0.001,
                        1e-4
                    )
                    index(2).isCloseTo(
                        1_000_000.0 + 100.0 + 0.002 +
                                2_000_000.0 + 150.0 + 0.002,
                        1e-4
                    )
                    index(3).isCloseTo(
                        1_000_000.0 + 100.0 + 0.003 +
                                2_000_000.0 + 150.0 + 0.003,
                        1e-4
                    )
                    index(4).isCloseTo(
                        1_000_000.0 + 100.0 + 0.004 +
                                2_000_000.0 + 150.0 + 0.004,
                        1e-4
                    )
                    index(5).isCloseTo(
                        1_000_000.0 + 100.0 + 0.005 +
                                2_000_000.0 + 150.0 + 0.005,
                        1e-4
                    )
                }
                index(1).containsExactly(ZeroSample, ZeroSample)
                index(2).all {
                    index(0).isCloseTo(
                        1_000_000.0 + 300.0 + 0.000,
                        1e-4
                    )
                    index(1).isCloseTo(
                        1_000_000.0 + 300.0 + 0.001,
                        1e-4
                    )
                }
                index(3).all {
                    index(0).isCloseTo(
                        1_000_000.0 + 300.0 + 0.002,
                        1e-4
                    )
                    index(1).isCloseTo(
                        1_000_000.0 + 300.0 + 0.003 +
                                2_000_000.0 + 350.0 + 0.000,
                        1e-4
                    )
                }
            }
        }
    }
})

private fun synthesizerStream(midiNotes: List<PolyphonicMidiChunk>) =
    PolyphonicSynthesizerStream(
        input = object : BeanStream<PolyphonicMidiChunk> {
            override val parameters: BeanParams
                get() = throw UnsupportedOperationException("not required")

            override fun inputs(): List<AnyBean> {
                throw UnsupportedOperationException("not required")
            }

            override val desiredSampleRate: Float?
                get() = throw UnsupportedOperationException("not required")

            override fun asSequence(sampleRate: Float): Sequence<PolyphonicMidiChunk> {
                return midiNotes.asSequence()
            }

        },
        parameters = PolyphonicSynthesizerStreamParams(
            mapOf(
                "1" to BaseVoice(1_000_000.0),
                "2" to BaseVoice(2_000_000.0),
            )
        )
    )

private class BaseVoice(val base: Double) : Voice() {
    override fun getSequence(frequency: Float, amplitude: Double, sampleOffset: Long): Sequence<Sample> {
        var offset = sampleOffset
        return object : Iterator<Sample> {
            override fun hasNext(): Boolean = true

            override fun next(): Sample = base + offset++ / 1000.0 + frequency

        }.asSequence()

    }
}