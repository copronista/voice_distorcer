package com.voicedistorcer.processing

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tanh

data class DistortionConfig(
    val pitchSemitones: Double = -4.0,
    val formantSemitones: Double = 2.0,
    val speed: Double = 1.0,
    val grit: Double = 0.0,
    val robot: Double = 0.0,
)

class VoiceDistortionProcessor(
    private val config: DistortionConfig = DistortionConfig(),
) {
    private val pitchShifter = PitchShifter()
    private val formantShifter = FormantShifter()
    private val tempoShifter = PhaseVocoder()

    fun process(input: ShortArray, sampleRate: Int = DEFAULT_SAMPLE_RATE): ShortArray {
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

        val robot = config.robot.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (robot > 0.0 && processed.isNotEmpty()) {
            processed = applyRobot(processed, robot, sampleRate)
        }

        val grit = config.grit.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (grit > 0.0 && processed.isNotEmpty()) {
            processed = applyGrit(processed, grit)
        }

        if (processed.isEmpty()) return ShortArray(0)

        return ShortArray(processed.size) { index ->
            (processed[index] * 32767.0).roundToInt().coerceIn(-32768, 32767).toShort()
        }
    }

    private fun applyRobot(input: DoubleArray, amount: Double, sampleRate: Int): DoubleArray {
        if (sampleRate <= 0) return input

        val phaseStep = 2.0 * PI * ROBOT_CARRIER_HZ / sampleRate
        return DoubleArray(input.size) { index ->
            val dry = input[index]
            val modulated = dry * sin(phaseStep * index)
            dry * (1.0 - amount) + modulated * amount
        }
    }

    private fun applyGrit(input: DoubleArray, amount: Double): DoubleArray {
        var peak = 0.0
        for (sample in input) {
            val magnitude = abs(sample)
            if (magnitude > peak) peak = magnitude
        }
        if (peak < MIN_PEAK) return input

        val drive = 1.0 + amount * MAX_GRIT_DRIVE
        val normalization = tanh(drive * peak) / peak
        return DoubleArray(input.size) { index ->
            val dry = input[index]
            val saturated = tanh(dry * drive) / normalization
            dry * (1.0 - amount) + saturated * amount
        }
    }

    private companion object {
        const val MIN_SPEED = 0.5
        const val MAX_SPEED = 1.5
        const val MIN_EFFECT = 0.0
        const val MAX_EFFECT = 1.0
        const val MAX_GRIT_DRIVE = 4.0
        const val MIN_PEAK = 1e-9
        const val ROBOT_CARRIER_HZ = 80.0
        const val DEFAULT_SAMPLE_RATE = 44_100
    }
}
