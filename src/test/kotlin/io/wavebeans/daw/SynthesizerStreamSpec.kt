package io.wavebeans.daw

import assertk.assertThat
import assertk.assertions.containsExactly
import io.wavebeans.lib.AnyBean
import io.wavebeans.lib.BeanParams
import io.wavebeans.lib.BeanStream
import io.wavebeans.lib.Fn
import io.wavebeans.lib.Sample
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class SynthesizerStreamSpec : Spek({
    describe("Synthesizing numbers") {

        fun synthesizerStream(midiNotes: List<MidiBuffer>) =
            SynthesizerStream(
                input = object : BeanStream<MidiBuffer> {
                    override val parameters: BeanParams
                        get() = throw UnsupportedOperationException("not required")

                    override fun inputs(): List<AnyBean> {
                        throw UnsupportedOperationException("not required")
                    }

                    override val desiredSampleRate: Float?
                        get() = throw UnsupportedOperationException("not required")

                    override fun asSequence(sampleRate: Float): Sequence<MidiBuffer> {
                        return midiNotes.asSequence()
                    }

                },
                parameters = SynthesizerStreamParams(NumericVoice)
            )

        it("should synthesize a note") {
            val samples = synthesizerStream(
                listOf(
                    MidiBuffer(
                        listOf(
                            StartNote(440.0f, 0),
                        ),
                        4
                    ),
                )
            ).asSequence(100.0f).flatMap { it.asList() }.toList()
            assertThat(samples).containsExactly(
                440.000,
                440.001,
                440.002,
                440.003
            )
        }
        it("should synthesize a note and then mute it") {
            val samples = synthesizerStream(
                listOf(
                    MidiBuffer(
                        listOf(
                            StartNote(440.0f, 0),
                            EndNote(3)
                        ),
                        4
                    ),
                )
            ).asSequence(100.0f).flatMap { it.asList() }.toList()
            assertThat(samples).containsExactly(
                440.000,
                440.001,
                440.002,
                000.000
            )
        }
        it("should synthesize a note and then change it") {
            val samples = synthesizerStream(
                listOf(
                    MidiBuffer(
                        listOf(
                            StartNote(440.0f, 0),
                            KeepNote(220.0f, 2)
                        ),
                        4
                    ),
                )
            ).asSequence(100.0f).flatMap { it.asList() }.toList()
            assertThat(samples).containsExactly(
                440.000,
                440.001,
                220.002,
                220.003
            )
        }
        it("should synthesize a note and then start over") {
            val samples = synthesizerStream(
                listOf(
                    MidiBuffer(
                        listOf(
                            StartNote(440.0f, 0),
                            StartNote(220.0f, 2)
                        ),
                        4
                    ),
                )
            ).asSequence(100.0f).flatMap { it.asList() }.toList()
            assertThat(samples).containsExactly(
                440.000,
                440.001,
                220.000,
                220.001
            )
        }
        it("should synthesize a note and then start over in a second buffer") {
            val samples = synthesizerStream(
                listOf(
                    MidiBuffer(
                        listOf(
                            StartNote(440.0f, 0),
                        ),
                        2
                    ),
                    MidiBuffer(
                        listOf(
                            StartNote(220.0f, 0)
                        ),
                        2
                    ),
                )
            ).asSequence(100.0f).flatMap { it.asList() }.toList()
            assertThat(samples).containsExactly(
                440.000,
                440.001,
                220.000,
                220.001
            )
        }
        it("should synthesize a note then continue in a second buffer and end in the third") {
            val samples = synthesizerStream(
                listOf(
                    MidiBuffer(
                        listOf(
                            StartNote(440.0f, 0),
                        ),
                        2
                    ),
                    MidiBuffer(
                        listOf(
                            KeepNote(220.0f, 0)
                        ),
                        2
                    ),
                    MidiBuffer(
                        listOf(
                            EndNote(0)
                        ),
                        2
                    ),
                )
            ).asSequence(100.0f).flatMap { it.asList() }.toList()
            assertThat(samples).containsExactly(
                440.000,
                440.001,
                220.002,
                220.003,
                000.000,
                000.000
            )
        }
        it("should synthesize a note then continue in a second buffer") {
            val samples = synthesizerStream(
                listOf(
                    MidiBuffer(
                        listOf(
                            StartNote(440.0f, 0),
                        ),
                        2
                    ),
                    MidiBuffer(
                        emptyList(),
                        2
                    ),
                )
            ).asSequence(100.0f).flatMap { it.asList() }.toList()
            assertThat(samples).containsExactly(
                440.000,
                440.001,
                440.002,
                440.003
            )
        }
    }
})

private object NumericVoice : Voice() {
    override fun getSequence(frequency: Float, amplitude: Double, sampleOffset: Long): Sequence<Sample> {
        var offset = sampleOffset
        return object : Iterator<Sample> {
            override fun hasNext(): Boolean = true

            override fun next(): Sample = offset++ / 1000.0 + frequency

        }.asSequence()

    }
}
