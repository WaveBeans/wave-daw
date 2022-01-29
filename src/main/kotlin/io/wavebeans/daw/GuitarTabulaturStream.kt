package io.wavebeans.daw

import io.wavebeans.lib.BeanParams
import io.wavebeans.lib.BeanStream
import io.wavebeans.lib.SourceBean
import kotlin.math.floor

fun String.tab(
    tuning: GuitarTuning,
    bpm: Float,
    beat: Pair<Int, Int>,
    timeSignature: Pair<Int, Int>
): BeanStream<PolyphonicMidiBuffer> =
    GuitarTabulaturStream(GuitarTabulaturStreamParams(tuning, bpm, this, beat, timeSignature))

class GuitarTabulaturStreamParams(
    val tuning: GuitarTuning,
    val bpm: Float,
    val tab: String,
    val stepMeasure: Pair<Int, Int>,
    val size: Pair<Int, Int>
) : BeanParams()

class GuitarTabulaturStream(
    override val parameters: GuitarTabulaturStreamParams

) : BeanStream<PolyphonicMidiBuffer>, SourceBean<PolyphonicMidiBuffer> {

    override val desiredSampleRate: Float? = null

    override fun asSequence(sampleRate: Float): Sequence<PolyphonicMidiBuffer> {
        val tabulatur = GuitarTabulatur.parse(parameters.tab)
        var mistake = 0.0
        val stepLengthSec = 60.0 / (
                parameters.bpm.toDouble() *
                        parameters.stepMeasure.second.toDouble() /
                        (parameters.size.second.toDouble() * parameters.stepMeasure.first.toDouble())
                )
        var stepLengthSamples = floor(stepLengthSec * sampleRate).toInt()
        mistake += stepLengthSec * sampleRate - stepLengthSamples
        val fullSamplesMistake = floor(mistake)
        if (fullSamplesMistake > 0) {
            stepLengthSamples += fullSamplesMistake.toInt()
            mistake -= fullSamplesMistake
        }

        val sounds = HashMap<String, Float?>()
        return tabulatur.steps.asSequence()
            .mapNotNull { step ->
                if (step is Notes) {
                    PolyphonicMidiBuffer(
                        step.motions.keys.associateWith { key ->
                            val list = ArrayList<MidiEvent>()
                            val base = parameters.tuning.getTune().getValue(key)
                            val guitarMotion = step.motions[key]
                            if (guitarMotion == null && sounds[key] != null) {
                                list.add(EndNote(0))
                            }
                            when (guitarMotion) {
                                is PickNote -> {
                                    val note = base.shift(guitarMotion.fret)
                                    list.add(StartNote(note, 0))
                                    sounds[key] = note
                                }
                                is KeepRinging -> {
                                    val note = sounds[key]
                                    if (note != null) {
                                        list.add(KeepNote(note, 0))
                                    }
                                }
                                is Mute -> {
                                    list.add(EndNote(0))
                                }
                            }
                            list
                        },
                        stepLengthSamples
                    )
                } else {
                    null
                }
            }
    }
}
