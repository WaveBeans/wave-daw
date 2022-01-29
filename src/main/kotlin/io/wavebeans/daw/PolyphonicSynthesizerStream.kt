package io.wavebeans.daw

import io.wavebeans.lib.AlterBean
import io.wavebeans.lib.AnyBean
import io.wavebeans.lib.BeanParams
import io.wavebeans.lib.BeanStream
import io.wavebeans.lib.NoParams
import io.wavebeans.lib.SampleVector
import io.wavebeans.lib.ZeroSample
import io.wavebeans.lib.plus
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import kotlin.collections.HashMap

class PolyphonicSynthesizerStreamParams(
    val generators: Map<VoiceKey, Voice>
) : BeanParams()

class PolyphonicSynthesizerStream(
    override val input: BeanStream<PolyphonicMidiBuffer>,
    override val parameters: PolyphonicSynthesizerStreamParams
) : BeanStream<SampleVector>, AlterBean<PolyphonicMidiBuffer, SampleVector> {

    override val desiredSampleRate: Float? = null

    override fun asSequence(sampleRate: Float): Sequence<SampleVector> {
        val voicesStreams = HashMap<VoiceKey, Iterator<SampleVector>>()
        val eventsForVoices = HashMap<VoiceKey, Queue<MidiBuffer>>()
        return input.asSequence(sampleRate).asSequence()
            .map { buffer: PolyphonicMidiBuffer ->
                buffer.events.entries.asSequence()
                    .map { (voiceKey, events: List<MidiEvent>) ->
                        val generator = requireNotNull(parameters.generators[voiceKey]) {
                            "$voiceKey is not found among synthesizers"
                        }
                        val voiceStream = voicesStreams.getOrPut(voiceKey) {
                            SynthesizerStream(
                                input = object : BeanStream<MidiBuffer> {
                                    override val parameters: BeanParams = NoParams()

                                    override fun inputs(): List<AnyBean> = emptyList()

                                    override val desiredSampleRate: Float? = null

                                    override fun asSequence(sampleRate: Float): Sequence<MidiBuffer> {
                                        return object : Iterator<MidiBuffer> {
                                            override fun hasNext(): Boolean = true

                                            override fun next(): MidiBuffer {
                                                val queue = requireNotNull(eventsForVoices[voiceKey]) {
                                                    "No events for voice=$voiceKey but asked for them"
                                                }
                                                if (queue.isEmpty()) {
                                                    throw IllegalStateException("Attempted to read more than provided for voice=$voiceKey")
                                                }
                                                return queue.poll()
                                            }

                                        }.asSequence()
                                    }

                                },
                                parameters = SynthesizerStreamParams(generator)
                            ).asSequence(sampleRate).iterator()
                        }
                        val eventsQueue = eventsForVoices.getOrPut(voiceKey) { ArrayBlockingQueue(1) }
                        eventsQueue.add(MidiBuffer(events, buffer.length))
                        voiceStream.next()
                    }
                    .fold(SampleVector(buffer.length) { ZeroSample }) { acc, v -> acc + v }
            }
    }

}
