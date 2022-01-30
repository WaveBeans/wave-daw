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
import kotlin.math.absoluteValue

fun BeanStream<PolyphonicMidiChunk>.synthesize(
    voices: Map<VoiceKey, Voice>,
    normalize: Boolean = true,
    voiceFadeInOut: Double = 0.005,
): BeanStream<SampleVector> =
    PolyphonicSynthesizerStream(this, PolyphonicSynthesizerStreamParams(voices, normalize, voiceFadeInOut))

class PolyphonicSynthesizerStreamParams(
    val generators: Map<VoiceKey, Voice>,
    val normalize: Boolean,
    val voiceFadeInOut: Double,
) : BeanParams()

class PolyphonicSynthesizerStream(
    override val input: BeanStream<PolyphonicMidiChunk>,
    override val parameters: PolyphonicSynthesizerStreamParams
) : BeanStream<SampleVector>, AlterBean<PolyphonicMidiChunk, SampleVector> {

    override val desiredSampleRate: Float? = null

    override fun asSequence(sampleRate: Float): Sequence<SampleVector> {
        val voicesStreams = HashMap<VoiceKey, Iterator<SampleVector>>()
        val eventsForVoices = HashMap<VoiceKey, Queue<MidiChunk>>()
        return input.asSequence(sampleRate).asSequence()
            .map { chunk: PolyphonicMidiChunk ->
                val vector = chunk.events.entries.asSequence()
                    .map { (voiceKey, events: List<MidiEvent>) ->
                        val generator = requireNotNull(parameters.generators[voiceKey]) {
                            "$voiceKey is not found among synthesizers"
                        }
                        val voiceStream = voicesStreams.getOrPut(voiceKey) {
                            SynthesizerStream(
                                input = object : BeanStream<MidiChunk> {
                                    override val parameters: BeanParams = NoParams()

                                    override fun inputs(): List<AnyBean> = emptyList()

                                    override val desiredSampleRate: Float? = null

                                    override fun asSequence(sampleRate: Float): Sequence<MidiChunk> {
                                        return object : Iterator<MidiChunk> {
                                            override fun hasNext(): Boolean = true

                                            override fun next(): MidiChunk {
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
                                parameters = SynthesizerStreamParams(generator, parameters.voiceFadeInOut)
                            ).asSequence(sampleRate).iterator()
                        }
                        val eventsQueue = eventsForVoices.getOrPut(voiceKey) { ArrayBlockingQueue(1) }
                        eventsQueue.add(MidiChunk(events, chunk.length))
                        voiceStream.next()
                    }
                    .fold(SampleVector(chunk.length) { ZeroSample }) { acc, v -> acc + v }
                if (parameters.normalize) {
                    val maxPeak = vector.asSequence().map { it.absoluteValue }.maxOrNull()!!
                    vector.indices.forEach { idx -> vector[idx] /= maxPeak }
                }
                vector
            }
    }
}
