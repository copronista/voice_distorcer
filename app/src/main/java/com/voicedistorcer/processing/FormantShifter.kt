package com.voicedistorcer.processing

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

internal class FormantShifter(
    private val fftSize: Int = 2048,
    private val hopSize: Int = 512,
    private val gainLimit: Double = 4.0,
    smoothing: Double = 0.5,
) {
    private val safeGainLimit = gainLimit.coerceIn(1.0, 8.0)
    private val envelopeCutoff = (
        fftSize / 32.0 * 2.0.pow((0.5 - smoothing.coerceIn(0.0, 1.0)) * 2.0)
    ).toInt().coerceIn(8, fftSize / 4)
    init {
        require(fftSize > 0 && (fftSize and (fftSize - 1)) == 0) {
            "FFT size must be a power of two"
        }
        require(hopSize in 1 until fftSize) { "Hop size must be smaller than the FFT size" }
    }

    fun shift(input: DoubleArray, semitones: Double): DoubleArray {
        if (input.isEmpty() || semitones == 0.0) return input.copyOf()

        val ratio = 2.0.pow(semitones / 12.0)
        val padded = DoubleArray(input.size + fftSize)
        input.copyInto(padded)
        val output = DoubleArray(padded.size)
        val windowSum = DoubleArray(padded.size)
        val window = DoubleArray(fftSize) { index ->
            0.5 - 0.5 * cos(2.0 * PI * index / (fftSize - 1))
        }

        val real = DoubleArray(fftSize)
        val imaginary = DoubleArray(fftSize)
        val magnitude = DoubleArray(fftSize / 2 + 1)
        val phase = DoubleArray(fftSize / 2 + 1)

        var position = 0
        while (position + fftSize <= padded.size) {
            for (index in 0 until fftSize) {
                real[index] = padded[position + index] * window[index]
                imaginary[index] = 0.0
            }
            Fft.transform(real, imaginary)

            for (bin in 0..fftSize / 2) {
                magnitude[bin] = hypot(real[bin], imaginary[bin])
                phase[bin] = atan2(imaginary[bin], real[bin])
            }

            val envelope = spectralEnvelope(magnitude)
            val warpedEnvelope = warpEnvelope(envelope, ratio)

            for (bin in 0..fftSize / 2) {
                val gain = (warpedEnvelope[bin] / max(envelope[bin], 1e-9))
                    .coerceIn(1.0 / safeGainLimit, safeGainLimit)
                val newMagnitude = magnitude[bin] * gain
                val newReal = newMagnitude * cos(phase[bin])
                val newImaginary = newMagnitude * sin(phase[bin])
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
                val outputIndex = position + index
                output[outputIndex] += real[index] * window[index]
                windowSum[outputIndex] += window[index] * window[index]
            }
            position += hopSize
        }

        for (index in output.indices) {
            if (windowSum[index] > 1e-8) {
                output[index] /= windowSum[index]
            }
        }
        return output.copyOf(input.size)
    }

    private fun spectralEnvelope(halfMagnitude: DoubleArray): DoubleArray {
        val real = DoubleArray(fftSize)
        val imaginary = DoubleArray(fftSize)

        for (bin in 0..fftSize / 2) {
            real[bin] = ln(halfMagnitude[bin] + 1e-9)
        }
        for (bin in 1 until fftSize / 2) {
            real[fftSize - bin] = real[bin]
        }

        Fft.transform(real, imaginary, inverse = true)

        for (index in envelopeCutoff until fftSize - envelopeCutoff) {
            real[index] = 0.0
            imaginary[index] = 0.0
        }

        Fft.transform(real, imaginary)
        return DoubleArray(fftSize / 2 + 1) { bin -> exp(real[bin]) }
    }

    private fun warpEnvelope(envelope: DoubleArray, ratio: Double): DoubleArray {
        val size = envelope.size
        return DoubleArray(size) { bin ->
            val source = bin / ratio
            when {
                source <= 0.0 -> envelope[0]
                source >= size - 1 -> envelope[size - 1]
                else -> {
                    val lowerIndex = floor(source).toInt()
                    val fraction = source - lowerIndex
                    envelope[lowerIndex] * (1.0 - fraction) +
                        envelope[lowerIndex + 1] * fraction
                }
            }
        }
    }
}
