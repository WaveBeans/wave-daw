package io.wavebeans.daw

import kotlin.math.pow

typealias Note = Float

const val E4: Note = 329.63f
const val B3: Note = 246.94f
const val G3: Note = 196.0f
const val D3: Note = 146.83f
const val A2: Note = 110.0f
const val E2: Note = 82.41f

fun Note.shift(halfTones: Int): Note = when {
    halfTones > 0 -> this * 2.0f.pow(halfTones / 12.0f)
    halfTones < 0 -> this / 2.0f.pow(-halfTones / 12.0f)
    halfTones == 0 -> this
    else -> throw UnsupportedOperationException("shift by $halfTones is unsupported")
}

interface GuitarTuning {
    /**
     * Gets the notes the guitar is tuned from the thinnest to the thickest strings.
     */
    fun getTune(): List<Note>
}

object Standard6StringTuning : GuitarTuning {
    override fun getTune(): List<Note> = listOf(E4, B3, G3, D3, A2, E2)
}