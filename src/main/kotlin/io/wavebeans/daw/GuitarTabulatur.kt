package io.wavebeans.daw

interface GuitarMotion

data class PickNote(
    val fret: Int
) : GuitarMotion

object KeepRinging : GuitarMotion {
    override fun toString(): String = "KeepRinging"
}

object Mute : GuitarMotion {
    override fun toString(): String = "Mute"
}

interface Step

object Bar : Step {
    override fun toString(): String = "Bar"
}

data class Notes(
    val motions: Map<VoiceKey, GuitarMotion>
) : Step

/**
 * Guitar tabulatur as a sequence of the [Step]s. Use [GuitarTabulatur.parse] to parse the text representation.
 */
data class GuitarTabulatur(
    val steps: List<Step>,
    val keys: List<String>
) {

    companion object {

        fun parse(tab: String): GuitarTabulatur {
            val measuresByKey = HashMap<String, MutableList<String>>()
            val keys = arrayListOf<String>()
            tab.lines().forEach { line ->
                when {
                    line.trim().startsWith("#") || line.trim().isBlank() -> {
                        // ignore
                    }
                    line.matches("^\\s*[a-zA-Z]\\|.*".toRegex()) -> {
                        val measures = line.split("|")
                        val key = measures[0].trim()
                        if (key !in keys) {
                            keys += key
                        }
                        val m = measuresByKey.getOrPut(key) { ArrayList() }
                        m.addAll(measures.drop(1))
                    }
                }
            }
            val measuresByIndex = ArrayList<HashMap<String, String>>()
            measuresByKey.entries.forEach { (key, value) ->
                value.forEachIndexed { idx, measure ->
                    while (idx >= measuresByIndex.size) {
                        measuresByIndex.add(HashMap())
                    }
                    val fullMeasure = measuresByIndex[idx]
                    fullMeasure[key] = measure
                }
            }

            val slices = ArrayList<MutableMap<String, String>>()
            measuresByIndex.forEach { measure ->
                var stepsSize = 2
                var index = 1
                var stepSlice = HashMap<String, String>()
                fun addToStepSlice(idx: Int) {
                    measure.entries
                        .map { it.key to it.value.elementAtOrNull(idx) }
                        .forEach { (key, slice) ->
                            if (slice != null) {
                                val currentSliceForKey = stepSlice.getOrPut(key) { "" }
                                if (slice != '-') {
                                    stepSlice[key] = currentSliceForKey + slice
                                }
                            }
                        }
                }

                addToStepSlice(0)
                while (true) {
                    val slice = measure.values.map { it.elementAtOrNull(index) }
                    if (slice.all { it != null }) {
                        addToStepSlice(index)
                        if (slice.all { it == '-' }) {
                            stepsSize = 1
                            index++
                            slices.add(stepSlice)
                            stepSlice = HashMap()
                            addToStepSlice(index)
                        }
                        index++
                        stepsSize++
                    } else {
                        break
                    }
                }
                slices.add(mutableMapOf())
            }

            return GuitarTabulatur(
                slices.map { slice ->
                    if (slice.isEmpty()) {
                        Bar
                    } else {
                        Notes(slice.map { (key, motion) ->
                            key to when {
                                motion.isBlank() -> Mute
                                motion.matches("\\d+".toRegex()) -> PickNote(motion.toInt())
                                else -> throw UnsupportedOperationException("Motion $motion is unsupported")
                            }
                        }.toMap())
                    }

                },
                keys
            )
        }
    }
}
