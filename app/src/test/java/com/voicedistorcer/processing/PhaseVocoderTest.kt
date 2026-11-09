package com.voicedistorcer.processing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class PhaseVocoderTest {
    @Test
    fun timeStretchChangesDurationAndPreservesFrequency() {
        val input = sineWave(sampleCount = 44_100, frequency = 440.0)
        val ratio = 1.5

        val output = PhaseVocoder().timeStretch(input, ratio)

        assertEquals((input.size * ratio), output.size.toDouble(), 1.0)
        assertEquals(440.0, estimateFrequency(output), 22.0)
    }

    @Test
    fun timeStretchKeepsSilenceQuiet() {
        val input = DoubleArray(44_100)

        val output = PhaseVocoder().timeStretch(input, 1.5)

        val peak = output.maxOfOrNull { abs(it) } ?: 0.0
        assertTrue("Peak was $peak", peak < 1e-6)
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
