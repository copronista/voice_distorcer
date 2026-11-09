package com.voicedistorcer.processing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class FormantShifterTest {
    @Test
    fun preservesDuration() {
        val input = harmonicSignal(sampleCount = 44_100)

        val output = FormantShifter().shift(input, 3.0)

        assertEquals(input.size, output.size)
    }

    @Test
    fun changesTheSpectralShape() {
        val input = harmonicSignal(sampleCount = 44_100)

        val output = FormantShifter().shift(input, 3.0)

        assertFalse(input.contentEquals(output))
    }

    @Test
    fun silenceRemainsQuiet() {
        val input = DoubleArray(44_100)

        val output = FormantShifter().shift(input, 3.0)

        val peak = output.maxOfOrNull { abs(it) } ?: 0.0
        assertTrue("Peak was $peak", peak < 1e-6)
    }

    private fun harmonicSignal(sampleCount: Int): DoubleArray =
        DoubleArray(sampleCount) { index ->
            var sample = 0.0
            for (harmonic in 1..20) {
                val frequency = 100.0 * harmonic
                val amplitude = if (frequency in 600.0..1000.0) 1.0 else 0.1
                sample += amplitude * sin(2.0 * PI * frequency * index / SAMPLE_RATE)
            }
            sample / 20.0
        }

    private companion object {
        const val SAMPLE_RATE = 44_100
    }
}
