package com.voicedistorcer.processing

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class FftTest {
    @Test
    fun forwardThenInverseRestoresSignal() {
        val size = 1024
        val real = DoubleArray(size) { index ->
            sin(2.0 * PI * 17 * index / size) + 0.25 * sin(2.0 * PI * 93 * index / size)
        }
        val imaginary = DoubleArray(size)
        val originalReal = real.copyOf()
        val originalImaginary = imaginary.copyOf()

        Fft.transform(real, imaginary)
        Fft.transform(real, imaginary, inverse = true)

        assertArrayEquals(originalReal, real, 1e-9)
        assertArrayEquals(originalImaginary, imaginary, 1e-9)
    }

    @Test
    fun impulseHasFlatSpectrum() {
        val size = 64
        val real = DoubleArray(size)
        val imaginary = DoubleArray(size)
        real[0] = 1.0

        Fft.transform(real, imaginary)

        for (bin in 0 until size) {
            assertEquals(1.0, real[bin], 1e-9)
            assertEquals(0.0, imaginary[bin], 1e-9)
        }
    }
}
