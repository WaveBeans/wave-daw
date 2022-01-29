package io.wavebeans.daw

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.key
import assertk.assertions.prop
import assertk.assertions.size
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class GuitarTabulaturStreamSpec : Spek({
    describe("6 string standard tuning guitar") {
        it("should interpret the two measures of 6/8 with 1/8 beat") {
            val tab = """
                # 1 2 3 4 5 6
                e|------0-----|
                B|----0---0---|
                G|--0-------0-|
                D|------------|
                A|------------|
                E|0-----------|
            """.tab(Standard6StringTuning, 120.0f, Pair(1, 8), Pair(6, 8))

            assertThat(tab.asSequence(1000.0f).toList()).all {
                size().isEqualTo(6)
                index(0).all {
                    prop(PolyphonicMidiChunk::length).isEqualTo(500)
                    prop(PolyphonicMidiChunk::events).all {
                        key("e").containsExactly(NoteOff(0))
                        key("B").containsExactly(NoteOff(0))
                        key("G").containsExactly(NoteOff(0))
                        key("D").containsExactly(NoteOff(0))
                        key("A").containsExactly(NoteOff(0))
                        key("E").containsExactly(NoteOn(E2, 0))
                    }
                }
                index(1).all {
                    prop(PolyphonicMidiChunk::length).isEqualTo(500)
                    prop(PolyphonicMidiChunk::events).all {
                        key("e").containsExactly(NoteOff(0))
                        key("B").containsExactly(NoteOff(0))
                        key("G").containsExactly(NoteOn(G3, 0))
                        key("D").containsExactly(NoteOff(0))
                        key("A").containsExactly(NoteOff(0))
                        key("E").containsExactly(NoteOff(0))
                    }
                }
                index(2).all {
                    prop(PolyphonicMidiChunk::length).isEqualTo(500)
                    prop(PolyphonicMidiChunk::events).all {
                        key("e").containsExactly(NoteOff(0))
                        key("B").containsExactly(NoteOn(B3, 0))
                        key("G").containsExactly(NoteOff(0))
                        key("D").containsExactly(NoteOff(0))
                        key("A").containsExactly(NoteOff(0))
                        key("E").containsExactly(NoteOff(0))
                    }
                }
                index(3).all {
                    prop(PolyphonicMidiChunk::length).isEqualTo(500)
                    prop(PolyphonicMidiChunk::events).all {
                        key("e").containsExactly(NoteOn(E4, 0))
                        key("B").containsExactly(NoteOff(0))
                        key("G").containsExactly(NoteOff(0))
                        key("D").containsExactly(NoteOff(0))
                        key("A").containsExactly(NoteOff(0))
                        key("E").containsExactly(NoteOff(0))
                    }
                }
                index(4).all {
                    prop(PolyphonicMidiChunk::length).isEqualTo(500)
                    prop(PolyphonicMidiChunk::events).all {
                        key("e").containsExactly(NoteOff(0))
                        key("B").containsExactly(NoteOn(B3, 0))
                        key("G").containsExactly(NoteOff(0))
                        key("D").containsExactly(NoteOff(0))
                        key("A").containsExactly(NoteOff(0))
                        key("E").containsExactly(NoteOff(0))
                    }
                }
                index(5).all {
                    prop(PolyphonicMidiChunk::length).isEqualTo(500)
                    prop(PolyphonicMidiChunk::events).all {
                        key("e").containsExactly(NoteOff(0))
                        key("B").containsExactly(NoteOff(0))
                        key("G").containsExactly(NoteOn(G3, 0))
                        key("D").containsExactly(NoteOff(0))
                        key("A").containsExactly(NoteOff(0))
                        key("E").containsExactly(NoteOff(0))
                    }
                }
            }
        }
    }
})

fun Assert<MidiChunk>.isEqualTo(length: Int, vararg events: MidiEvent) = all {
    prop(MidiChunk::events).containsExactly(*events)
    prop(MidiChunk::length).isEqualTo(length)
}