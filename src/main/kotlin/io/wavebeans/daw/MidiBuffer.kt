package io.wavebeans.daw

interface MidiEvent {
    val offset: Int
}

data class StartNote(
    val frequency: Float,
    override val offset: Int,
) : MidiEvent

data class EndNote(
    override val offset: Int
) : MidiEvent

data class KeepNote(
    val frequency: Float,
    override val offset: Int
) : MidiEvent


data class PolyphonicMidiBuffer(
    val events: Map<VoiceKey, List<MidiEvent>>,
    val length: Int
)

data class MidiBuffer(
    val events: List<MidiEvent>,
    val length: Int,
)