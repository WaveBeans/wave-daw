package io.wavebeans.daw

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.index
import assertk.assertions.isEqualTo
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
                    index(0).isEqualTo(500, EndNote(0))
                    index(1).isEqualTo(500, EndNote(0))
                    index(2).isEqualTo(500, EndNote(0))
                    index(3).isEqualTo(500, EndNote(0))
                    index(4).isEqualTo(500, EndNote(0))
                    index(5).isEqualTo(500, StartNote(E2, 0))
                }
                index(1).all {
                    index(0).isEqualTo(500, EndNote(0))
                    index(1).isEqualTo(500, EndNote(0))
                    index(2).isEqualTo(500, StartNote(G3, 0))
                    index(3).isEqualTo(500, EndNote(0))
                    index(4).isEqualTo(500, EndNote(0))
                    index(5).isEqualTo(500, EndNote(0))
                }
                index(2).all {
                    index(0).isEqualTo(500, EndNote(0))
                    index(1).isEqualTo(500, StartNote(B3, 0))
                    index(2).isEqualTo(500, EndNote(0))
                    index(3).isEqualTo(500, EndNote(0))
                    index(4).isEqualTo(500, EndNote(0))
                    index(5).isEqualTo(500, EndNote(0))
                }
                index(3).all {
                    index(0).isEqualTo(500, StartNote(E4, 0))
                    index(1).isEqualTo(500, EndNote(0))
                    index(2).isEqualTo(500, EndNote(0))
                    index(3).isEqualTo(500, EndNote(0))
                    index(4).isEqualTo(500, EndNote(0))
                    index(5).isEqualTo(500, EndNote(0))
                }
                index(4).all {
                    index(0).isEqualTo(500, EndNote(0))
                    index(1).isEqualTo(500, StartNote(B3, 0))
                    index(2).isEqualTo(500, EndNote(0))
                    index(3).isEqualTo(500, EndNote(0))
                    index(4).isEqualTo(500, EndNote(0))
                    index(5).isEqualTo(500, EndNote(0))
                }
                index(5).all {
                    index(0).isEqualTo(500, EndNote(0))
                    index(1).isEqualTo(500, EndNote(0))
                    index(2).isEqualTo(500, StartNote(G3, 0))
                    index(3).isEqualTo(500, EndNote(0))
                    index(4).isEqualTo(500, EndNote(0))
                    index(5).isEqualTo(500, EndNote(0))
                }
            }
        }
    }
})

fun Assert<MidiBuffer>.isEqualTo(length: Int, vararg events: MidiEvent) = all {
    prop(MidiBuffer::events).containsExactly(*events)
    prop(MidiBuffer::length).isEqualTo(length)
}