package com.voicedistorcer.processing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class VoiceDistortionProcessorTest {
    @Test
    fun preservesDuration() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val output = VoiceDistortionProcessor().process(input)

        assertEquals(input.size, output.size)
    }

    @Test
    fun changesTheSignal() {
        val input = sineWave(sampleCount = 8820, frequency = 220.0)

        val output = VoiceDistortionProcessor().process(input)

        assertFalse(input.contentEquals(output))
    }

    @Test
    fun pitchShiftLowersTheFundamentalFrequency() {
        val inputFrequency = 440.0
        val input = sineWave(sampleCount = 44_100, frequency = inputFrequency)

        val output = VoiceDistortionProcessor().process(input)

        val outputFrequency = estimateFrequency(output)
        val expectedFrequency = inputFrequency * Math.pow(2.0, -4.0 / 12.0)
        assertEquals(expectedFrequency, outputFrequency, expectedFrequency * 0.05)
    }

    @Test
    fun emptyInputProducesEmptyOutput() {
        val output = VoiceDistortionProcessor().process(ShortArray(0))

        assertEquals(0, output.size)
    }

    @Test
    fun zeroShiftConfigPreservesTheSignal() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(pitchSemitones = 0.0, formantSemitones = 0.0),
        ).process(input)

        assertEquals(input.size, output.size)
        for (index in input.indices) {
            val difference = abs(input[index].toInt() - output[index].toInt())
            assertTrue("Sample $index differed by $difference", difference <= 1)
        }
    }

    @Test
    fun silenceRemainsQuiet() {
        val input = ShortArray(4410)

        val output = VoiceDistortionProcessor().process(input)

        assertEquals(input.size, output.size)
        val peak = output.maxOfOrNull { abs(it.toInt()) } ?: 0
        assertTrue("Peak was $peak", peak < 32)
    }

    private fun sineWave(sampleCount: Int, frequency: Double): ShortArray =
        ShortArray(sampleCount) { index ->
            (sin(2.0 * PI * frequency * index / SAMPLE_RATE) * 20_000.0).toInt().toShort()
        }

    private fun estimateFrequency(samples: ShortArray): Double {
        val start = samples.size / 4
        val end = samples.size * 3 / 4
        var crossings = 0
        for (index in start + 1 until end) {
            val previous = samples[index - 1].toInt()
            val current = samples[index].toInt()
            if ((previous < 0 && current >= 0) || (previous >= 0 && current < 0)) {
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
