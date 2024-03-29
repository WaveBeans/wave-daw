package io.wavebeans.daw

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.each
import assertk.assertions.isCloseTo
import io.wavebeans.lib.AnyBean
import io.wavebeans.lib.BeanParams
import io.wavebeans.lib.BeanStream
import io.wavebeans.lib.Sample
import io.wavebeans.lib.io.sine
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class SynthesizerStreamSpec : Spek({
    describe("Synthesizing numbers") {


        it("should synthesize a note") {
            val samples = synthesizerStream(
                listOf(
                    MidiChunk(
                        listOf(
                            NoteOn(440.0f, 0),
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
                    MidiChunk(
                        listOf(
                            NoteOn(440.0f, 0),
                            NoteOff(3)
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
                    MidiChunk(
                        listOf(
                            NoteOn(440.0f, 0),
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
                    MidiChunk(
                        listOf(
                            NoteOn(440.0f, 0),
                            NoteOn(220.0f, 2)
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
        it("should synthesize a note and then start over in a second buffer") {
            val samples = synthesizerStream(
                listOf(
                    MidiChunk(
                        listOf(
                            NoteOn(440.0f, 0),
                        ),
                        2
                    ),
                    MidiChunk(
                        listOf(
                            NoteOn(220.0f, 0)
                        ),
                        2
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
        it("should synthesize a note then continue in a second buffer and end in the third") {
            val samples = synthesizerStream(
                listOf(
                    MidiChunk(
                        listOf(
                            NoteOn(440.0f, 0),
                        ),
                        2
                    ),
                    MidiChunk(
                        listOf(
                            KeepNote(220.0f, 0)
                        ),
                        2
                    ),
                    MidiChunk(
                        listOf(
                            NoteOff(0)
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
        it("should synthesize a note then continue in a second buffer and end in the third, in the forth keep the through offset") {
            val samples = synthesizerStream(
                listOf(
                    MidiChunk(
                        listOf(
                            NoteOn(440.0f, 0),
                        ),
                        2
                    ),
                    MidiChunk(
                        listOf(
                            KeepNote(220.0f, 0)
                        ),
                        2
                    ),
                    MidiChunk(
                        listOf(
                            NoteOff(0)
                        ),
                        2
                    ),
                    MidiChunk(
                        listOf(
                            NoteOn(110.0f, 0)
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
                000.000,
                110.004,
                110.005
            )
        }
        it("should synthesize a note then continue in a second buffer") {
            val samples = synthesizerStream(
                listOf(
                    MidiChunk(
                        listOf(
                            NoteOn(440.0f, 0),
                        ),
                        2
                    ),
                    MidiChunk(
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
    describe("Sine synth") {
        it("should synthesize sines") {
            val sampleRate = 100.0f
            val samples = synthesizerStream(
                listOf(
                    MidiChunk(
                        listOf(
                            NoteOn(40.0f, 0),
                        ),
                        50
                    ),
                    MidiChunk(
                        listOf(
                            KeepNote(40.0f, 0),
                        ),
                        50
                    ),
                    MidiChunk(
                        listOf(
                            KeepNote(30.0f, 0),
                        ),
                        50
                    ),
                ),
                voice = SineVoice()
            ).asSequence(sampleRate).flatMap { it.asList() }.toList()

            val expected = (
                    40.sine(amplitude = 1.0).asSequence(sampleRate).take(100) +
                            30.sine(amplitude = 1.0).asSequence(sampleRate).take(50)
                    ).iterator()

            assertThat(samples)
                .each { it.isCloseTo(expected.next(), 1e-10) }
        }
    }
})

fun synthesizerStream(midiNotes: List<MidiChunk>, voice: Voice = NumericVoice) =
    SynthesizerStream(
        input = object : BeanStream<MidiChunk> {
            override val parameters: BeanParams
                get() = throw UnsupportedOperationException("not required")

            override fun inputs(): List<AnyBean> {
                throw UnsupportedOperationException("not required")
            }

            override val desiredSampleRate: Float?
                get() = throw UnsupportedOperationException("not required")

            override fun asSequence(sampleRate: Float): Sequence<MidiChunk> {
                return midiNotes.asSequence()
            }

        },
        parameters = SynthesizerStreamParams(voice, 0.0)
    )

private object NumericVoice : Voice() {
    override fun getSequence(
        frequency: Float,
        amplitude: Double,
        sampleOffset: Long,
        sampleRate: Float
    ): Sequence<Sample> {
        var offset = sampleOffset
        return object : Iterator<Sample> {
            override fun hasNext(): Boolean = true

            override fun next(): Sample {
                if (!hasNext()) throw NoSuchElementException()
                return offset++ / 1000.0 + frequency
            }

        }.asSequence()

    }
}
