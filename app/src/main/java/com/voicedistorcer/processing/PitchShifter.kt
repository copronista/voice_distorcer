package com.voicedistorcer.processing

import kotlin.math.floor
import kotlin.math.pow

internal class PitchShifter(
    private val vocoder: PhaseVocoder = PhaseVocoder(),
) {
    fun shift(input: DoubleArray, semitones: Double): DoubleArray {
        if (input.isEmpty() || semitones == 0.0) return input.copyOf()

        val factor = 2.0.pow(semitones / 12.0)
        val stretched = vocoder.timeStretch(input, factor)
        return resample(stretched, factor, input.size)
    }

    private fun resample(input: DoubleArray, factor: Double, targetLength: Int): DoubleArray {
        if (input.isEmpty()) return DoubleArray(targetLength)

        val output = DoubleArray(targetLength)
        for (index in output.indices) {
            val sourcePosition = index * factor
            val lowerIndex = floor(sourcePosition).toInt()
            val fraction = sourcePosition - lowerIndex
            val lowerSample = input.getOrElse(lowerIndex) { input.last() }
            val upperSample = input.getOrElse(lowerIndex + 1) { input.last() }
            output[index] = lowerSample + (upperSample - lowerSample) * fraction
        }
        return output
    }
}
