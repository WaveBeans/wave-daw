package io.wavebeans.daw

import io.wavebeans.lib.AnyBean
import io.wavebeans.lib.BeanParams
import io.wavebeans.lib.BeanStream

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

data class MidiBuffer(
    val events: List<MidiEvent>,
    val length: Int,
)

class MidiStream(
    override val parameters: BeanParams
) : BeanStream<List<MidiEvent>> {

    override val desiredSampleRate: Float? = null

    override fun inputs(): List<AnyBean> {
        TODO("Not yet implemented")
    }

    override fun asSequence(sampleRate: Float): Sequence<List<MidiEvent>> {
        TODO("Not yet implemented")
    }
}