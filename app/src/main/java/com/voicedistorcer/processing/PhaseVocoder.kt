package com.voicedistorcer.processing

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

internal class PhaseVocoder(
    private val fftSize: Int = 2048,
    private val analysisHop: Int = 512,
) {
    init {
        require(fftSize > 0 && (fftSize and (fftSize - 1)) == 0) {
            "FFT size must be a power of two"
        }
        require(analysisHop in 1 until fftSize / 2) {
            "Analysis hop must be smaller than half the FFT size"
        }
    }

    fun timeStretch(input: DoubleArray, ratio: Double): DoubleArray {
        if (input.isEmpty() || ratio == 1.0) return input.copyOf()
        require(ratio > 0.0) { "Stretch ratio must be positive" }

        val synthesisHop = max(1, (analysisHop * ratio).roundToInt())
        val padded = DoubleArray(input.size + fftSize)
        input.copyInto(padded)

        val targetLength = ceil(input.size * ratio).toInt()
        val outputLength = targetLength + fftSize
        val output = DoubleArray(outputLength)
        val windowSum = DoubleArray(outputLength)
        val window = hannWindow(fftSize)

        val real = DoubleArray(fftSize)
        val imaginary = DoubleArray(fftSize)
        val lastPhase = DoubleArray(fftSize / 2 + 1)
        val sumPhase = DoubleArray(fftSize / 2 + 1)
        val expectedAdvance = DoubleArray(fftSize / 2 + 1) { bin ->
            2.0 * PI * bin * analysisHop / fftSize
        }

        var analysisPosition = 0
        var synthesisPosition = 0
        while (analysisPosition + fftSize <= padded.size &&
            synthesisPosition + fftSize <= outputLength
        ) {
            for (index in 0 until fftSize) {
                real[index] = padded[analysisPosition + index] * window[index]
                imaginary[index] = 0.0
            }
            Fft.transform(real, imaginary)

            for (bin in 0..fftSize / 2) {
                val magnitude = hypot(real[bin], imaginary[bin])
                val phase = atan2(imaginary[bin], real[bin])
                val deviation = wrapToPi(phase - lastPhase[bin] - expectedAdvance[bin])
                val trueAdvance = expectedAdvance[bin] + deviation
                lastPhase[bin] = phase
                sumPhase[bin] += trueAdvance * synthesisHop / analysisHop

                val newReal = magnitude * cos(sumPhase[bin])
                val newImaginary = magnitude * sin(sumPhase[bin])
                real[bin] = newReal
                imaginary[bin] = newImaginary
                if (bin > 0) {
                    real[fftSize - bin] = newReal
                    imaginary[fftSize - bin] = -newImaginary
                }
            }
            imaginary[0] = 0.0
            imaginary[fftSize / 2] = 0.0

            Fft.transform(real, imaginary, inverse = true)

            for (index in 0 until fftSize) {
                val outputIndex = synthesisPosition + index
                output[outputIndex] += real[index] * window[index]
                windowSum[outputIndex] += window[index] * window[index]
            }

            analysisPosition += analysisHop
            synthesisPosition += synthesisHop
        }

        for (index in output.indices) {
            if (windowSum[index] > 1e-8) {
                output[index] /= windowSum[index]
            }
        }

        return output.copyOf(targetLength.coerceAtMost(output.size))
    }

    private fun hannWindow(size: Int): DoubleArray = DoubleArray(size) { index ->
        0.5 - 0.5 * cos(2.0 * PI * index / (size - 1))
    }

    private fun wrapToPi(value: Double): Double {
        var wrapped = value
        while (wrapped > PI) wrapped -= 2.0 * PI
        while (wrapped < -PI) wrapped += 2.0 * PI
        return wrapped
    }
}
