package com.voicedistorcer.processing

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class PitchShifterTest {
    @Test
    fun shiftingUpByAnOctaveDoublesTheFrequency() {
        val input = sineWave(sampleCount = 44_100, frequency = 220.0)

        val output = PitchShifter().shift(input, 12.0)

        assertEquals(input.size, output.size)
        assertEquals(440.0, estimateFrequency(output), 22.0)
    }

    @Test
    fun shiftingDownByAnOctaveHalvesTheFrequency() {
        val input = sineWave(sampleCount = 44_100, frequency = 440.0)

        val output = PitchShifter().shift(input, -12.0)

        assertEquals(input.size, output.size)
        assertEquals(220.0, estimateFrequency(output), 11.0)
    }

    @Test
    fun zeroSemitonesReturnsAnIdenticalCopy() {
        val input = sineWave(sampleCount = 1024, frequency = 220.0)

        val output = PitchShifter().shift(input, 0.0)

        assertArrayEquals(input, output, 0.0)
    }

    private fun sineWave(sampleCount: Int, frequency: Double): DoubleArray =
        DoubleArray(sampleCount) { index ->
            sin(2.0 * PI * frequency * index / SAMPLE_RATE)
        }

    private fun estimateFrequency(samples: DoubleArray): Double {
        val start = samples.size / 4
        val end = samples.size * 3 / 4
        var crossings = 0
        for (index in start + 1 until end) {
            if ((samples[index - 1] < 0.0 && samples[index] >= 0.0) ||
                (samples[index - 1] >= 0.0 && samples[index] < 0.0)
            ) {
                crossings++
            }
        }
        val durationSeconds = (end - start).toDouble() / SAMPLE_RATE
        return crossings / (2.0 * durationSeconds)
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
    }
}
