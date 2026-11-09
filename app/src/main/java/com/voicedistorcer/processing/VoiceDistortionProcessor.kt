package com.voicedistorcer.processing

import kotlin.math.roundToInt

data class DistortionConfig(
    val pitchSemitones: Double = -4.0,
    val formantSemitones: Double = 2.0,
    val speed: Double = 1.0,
)

class VoiceDistortionProcessor(
    private val config: DistortionConfig = DistortionConfig(),
) {
    private val pitchShifter = PitchShifter()
    private val formantShifter = FormantShifter()
    private val tempoShifter = PhaseVocoder()

    fun process(input: ShortArray): ShortArray {
        if (input.isEmpty()) return ShortArray(0)

        var processed = DoubleArray(input.size) { index -> input[index] / 32768.0 }

        if (config.formantSemitones != 0.0) {
            processed = formantShifter.shift(processed, config.formantSemitones)
        }
        if (config.pitchSemitones != 0.0) {
            processed = pitchShifter.shift(processed, config.pitchSemitones)
        }

        val speed = config.speed.coerceIn(MIN_SPEED, MAX_SPEED)
        if (speed != 1.0 && processed.isNotEmpty()) {
            processed = tempoShifter.timeStretch(processed, 1.0 / speed)
        }
        if (processed.isEmpty()) return ShortArray(0)

        return ShortArray(processed.size) { index ->
            (processed[index] * 32767.0).roundToInt().coerceIn(-32768, 32767).toShort()
        }
    }

    private companion object {
        const val MIN_SPEED = 0.5
        const val MAX_SPEED = 1.5
    }
}
