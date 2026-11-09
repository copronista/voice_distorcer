package com.voicedistorcer.processing

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal object Fft {
    fun transform(real: DoubleArray, imaginary: DoubleArray, inverse: Boolean = false) {
        val size = real.size
        require(size == imaginary.size) { "Real and imaginary arrays must have the same size" }
        require(size > 0 && (size and (size - 1)) == 0) { "FFT size must be a power of two" }

        var reversed = 0
        for (index in 1 until size) {
            var bit = size shr 1
            while ((reversed and bit) != 0) {
                reversed = reversed xor bit
                bit = bit shr 1
            }
            reversed = reversed xor bit
            if (index < reversed) {
                val realSwap = real[index]
                real[index] = real[reversed]
                real[reversed] = realSwap

                val imaginarySwap = imaginary[index]
                imaginary[index] = imaginary[reversed]
                imaginary[reversed] = imaginarySwap
            }
        }

        var length = 2
        while (length <= size) {
            val angle = 2.0 * PI / length * if (inverse) 1.0 else -1.0
            val twiddleReal = cos(angle)
            val twiddleImaginary = sin(angle)
            var start = 0
            while (start < size) {
                var currentReal = 1.0
                var currentImaginary = 0.0
                val half = length / 2
                for (offset in 0 until half) {
                    val evenReal = real[start + offset]
                    val evenImaginary = imaginary[start + offset]
                    val oddReal = real[start + offset + half]
                    val oddImaginary = imaginary[start + offset + half]

                    val rotatedReal = oddReal * currentReal - oddImaginary * currentImaginary
                    val rotatedImaginary = oddReal * currentImaginary + oddImaginary * currentReal

                    real[start + offset] = evenReal + rotatedReal
                    imaginary[start + offset] = evenImaginary + rotatedImaginary
                    real[start + offset + half] = evenReal - rotatedReal
                    imaginary[start + offset + half] = evenImaginary - rotatedImaginary

                    val nextReal = currentReal * twiddleReal - currentImaginary * twiddleImaginary
                    currentImaginary = currentReal * twiddleImaginary + currentImaginary * twiddleReal
                    currentReal = nextReal
                }
                start += length
            }
            length = length shl 1
        }

        if (inverse) {
            for (index in 0 until size) {
                real[index] /= size
                imaginary[index] /= size
            }
        }
    }
}
