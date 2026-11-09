package com.voicedistorcer.processing

import kotlin.math.roundToInt

data class DistortionConfig(
    val pitchSemitones: Double = -4.0,
    val formantSemitones: Double = 2.0,
)

class VoiceDistortionProcessor(
    private val config: DistortionConfig = DistortionConfig(),
) {
    private val pitchShifter = PitchShifter()
    private val formantShifter = FormantShifter()
    fun process(input: ShortArray): ShortArray {
        if (input.isEmpty()) return ShortArray(0)

        var processed = DoubleArray(input.size) { index -> input[index] / 32768.0 }

        if (config.formantSemitones != 0.0) {
            processed = formantShifter.shift(processed, config.formantSemitones)
        }
        if (config.pitchSemitones != 0.0) {
            processed = pitchShifter.shift(processed, config.pitchSemitones)
        }

        return ShortArray(input.size) { index ->
            val sample = if (index < processed.size) processed[index] else 0.0
            (sample * 32767.0).roundToInt().coerceIn(-32768, 32767).toShort()
        }
    }
}
