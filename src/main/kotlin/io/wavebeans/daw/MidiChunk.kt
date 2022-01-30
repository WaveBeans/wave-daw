package io.wavebeans.daw

interface MidiEvent {
    val offset: Int
}

data class NoteOn(
    val frequency: Float,
    override val offset: Int,
) : MidiEvent

data class NoteOff(
    override val offset: Int
) : MidiEvent

data class KeepNote(
    val frequency: Float,
    override val offset: Int
) : MidiEvent


data class PolyphonicMidiChunk(
    val events: Map<VoiceKey, List<MidiEvent>>,
    val length: Int
)

data class MidiChunk(
    val events: List<MidiEvent>,
    val length: Int,
)