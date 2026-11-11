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
    fun formantShiftChangesTheSignalAndPreservesDuration() {
        val input = sineWave(sampleCount = 8820, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(pitchSemitones = 0.0, formantSemitones = 4.0),
        ).process(input)

        assertEquals(input.size, output.size)
        assertFalse(input.contentEquals(output))
    }

    @Test
    fun speedAboveOneShortensTheClip() {
        val input = sineWave(sampleCount = 44_100, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(speed = 1.5),
        ).process(input)

        val expectedLength = input.size / 1.5
        assertEquals(expectedLength.toDouble(), output.size.toDouble(), 2.0)
    }

    @Test
    fun speedBelowOneLengthensTheClip() {
        val input = sineWave(sampleCount = 44_100, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(speed = 0.5),
        ).process(input)

        val expectedLength = input.size / 0.5
        assertEquals(expectedLength.toDouble(), output.size.toDouble(), 2.0)
    }

    @Test
    fun speedIsClampedToSupportedRange() {
        val input = sineWave(sampleCount = 44_100, frequency = 220.0)

        val tooFast = VoiceDistortionProcessor(DistortionConfig(speed = 10.0)).process(input)
        val clampedFast = VoiceDistortionProcessor(DistortionConfig(speed = 1.5)).process(input)
        val tooSlow = VoiceDistortionProcessor(DistortionConfig(speed = 0.01)).process(input)
        val clampedSlow = VoiceDistortionProcessor(DistortionConfig(speed = 0.5)).process(input)

        assertEquals(clampedFast.size, tooFast.size)
        assertEquals(clampedSlow.size, tooSlow.size)
    }

    @Test
    fun gritChangesTheSignalAndPreservesDuration() {
        val input = sineWave(sampleCount = 8820, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                grit = 0.8,
            ),
        ).process(input)

        assertEquals(input.size, output.size)
        assertFalse(input.contentEquals(output))
    }

    @Test
    fun gritPreservesTheInputPeakLevel() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0, amplitude = 3_000.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                grit = 1.0,
            ),
        ).process(input)

        val inputPeak = input.maxOf { abs(it.toInt()) }
        val outputPeak = output.maxOf { abs(it.toInt()) }
        assertEquals(inputPeak.toDouble(), outputPeak.toDouble(), 1.0)
    }

    @Test
    fun robotChangesTheSignalAndPreservesDuration() {
        val input = sineWave(sampleCount = 8820, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                robot = 0.8,
            ),
        ).process(input)

        assertEquals(input.size, output.size)
        assertFalse(input.contentEquals(output))
    }

    @Test
    fun echoAddsDelayedRepeats() {
        val input = ShortArray(44_100)
        input[0] = 10_000

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                echo = 1.0,
            ),
        ).process(input)

        val delayedPeak = output.drop(1000).maxOfOrNull { abs(it.toInt()) } ?: 0
        assertTrue("Delayed peak was $delayedPeak", delayedPeak > 1000)
    }

    @Test
    fun fanAddsNoiseToSilence() {
        val input = ShortArray(4410)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                fan = 1.0,
            ),
        ).process(input)

        val peak = output.maxOfOrNull { abs(it.toInt()) } ?: 0
        assertTrue("Peak was $peak", peak > 100)
    }

    @Test
    fun echoAndFanAreClampedToSupportedRange() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val tooMuch = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                echo = 10.0,
                fan = 10.0,
            ),
        ).process(input)
        val clamped = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                echo = 1.0,
                fan = 1.0,
            ),
        ).process(input)

        assertTrue(tooMuch.contentEquals(clamped))
    }

    @Test
    fun reverseReversesTheClip() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                reverse = true,
            ),
        ).process(input)

        val expected = input.reversedArray()
        assertEquals(input.size, output.size)
        for (index in expected.indices) {
            val difference = abs(expected[index].toInt() - output[index].toInt())
            assertTrue("Sample $index differed by $difference", difference <= 1)
        }
    }

    @Test
    fun stutterChangesTheSignalAndPreservesDuration() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                stutter = 1.0,
            ),
        ).process(input)

        assertEquals(input.size, output.size)
        assertFalse(input.contentEquals(output))
    }

    @Test
    fun alienChangesTheSignalAndPreservesDuration() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                alien = 0.8,
            ),
        ).process(input)

        assertEquals(input.size, output.size)
        assertFalse(input.contentEquals(output))
    }

    @Test
    fun drunkChangesTheSignalAndPreservesDuration() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                drunk = 0.8,
            ),
        ).process(input)

        assertEquals(input.size, output.size)
        assertFalse(input.contentEquals(output))
    }

    @Test
    fun telephoneAttenuatesLowFrequencies() {
        val input = sineWave(sampleCount = 4410, frequency = 100.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                telephone = 1.0,
            ),
        ).process(input)

        val inputPeak = input.maxOf { abs(it.toInt()) }
        val outputPeak = output.drop(1000).maxOf { abs(it.toInt()) }
        assertTrue("Input peak $inputPeak, output peak $outputPeak", outputPeak < inputPeak * 0.8)
    }

    @Test
    fun megaphoneChangesTheSignalAndPreservesDuration() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                megaphone = 1.0,
            ),
        ).process(input)

        assertEquals(input.size, output.size)
        assertFalse(input.contentEquals(output))
    }

    @Test
    fun beeChangesTheSignalAndPreservesDuration() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                bee = 0.8,
            ),
        ).process(input)

        assertEquals(input.size, output.size)
        assertFalse(input.contentEquals(output))
    }

    @Test
    fun funEffectsAreClampedToSupportedRange() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val tooMuch = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                stutter = 10.0,
                alien = 10.0,
                drunk = 10.0,
                telephone = 10.0,
                megaphone = 10.0,
                bee = 10.0,
            ),
        ).process(input)
        val clamped = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                stutter = 1.0,
                alien = 1.0,
                drunk = 1.0,
                telephone = 1.0,
                megaphone = 1.0,
                bee = 1.0,
            ),
        ).process(input)

        assertTrue(tooMuch.contentEquals(clamped))
    }

    @Test
    fun characterEffectsAreClampedToSupportedRange() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val tooMuch = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                grit = 10.0,
                robot = 10.0,
            ),
        ).process(input)
        val clamped = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                grit = 1.0,
                robot = 1.0,
            ),
        ).process(input)

        assertTrue(tooMuch.contentEquals(clamped))
    }

    @Test
    fun highPassFilterRemovesDcOffset() {
        val input = ShortArray(4410) { 10_000 }

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                highPassHz = 100.0,
            ),
        ).process(input)

        val peak = output.drop(1000).maxOfOrNull { abs(it.toInt()) } ?: 0
        assertTrue("Peak was $peak", peak < 100)
    }

    @Test
    fun noiseGateAttenuatesQuietSignals() {
        val input = sineWave(sampleCount = 22_050, frequency = 220.0, amplitude = 200.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                noiseGateDb = -30.0,
            ),
        ).process(input)

        val peak = output.drop(15_000).maxOfOrNull { abs(it.toInt()) } ?: 0
        assertTrue("Peak was $peak", peak < 20)
    }

    @Test
    fun compressorReducesLoudPeaks() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                compressor = 1.0,
            ),
        ).process(input)

        val inputPeak = input.drop(2000).maxOf { abs(it.toInt()) }
        val outputPeak = output.drop(2000).maxOf { abs(it.toInt()) }
        assertTrue("Input peak $inputPeak, output peak $outputPeak", outputPeak < inputPeak)
    }

    @Test
    fun outputGainScalesTheSignal() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0, amplitude = 5_000.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                outputGainDb = 6.0,
            ),
        ).process(input)

        val inputPeak = input.maxOf { abs(it.toInt()) }
        val outputPeak = output.maxOf { abs(it.toInt()) }
        val expectedPeak = inputPeak * Math.pow(10.0, 6.0 / 20.0)
        assertEquals(expectedPeak, outputPeak.toDouble(), 1.0)
    }

    @Test
    fun zeroFormantMixPreservesTheSignal() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 4.0,
                formantMix = 0.0,
            ),
        ).process(input)

        for (index in input.indices) {
            val difference = abs(input[index].toInt() - output[index].toInt())
            assertTrue("Sample $index differed by $difference", difference <= 1)
        }
    }

    @Test
    fun customFftSizesPreserveDuration() {
        val input = sineWave(sampleCount = 4410, frequency = 220.0)

        val output = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 2.0,
                formantSemitones = 2.0,
                formantFftSize = 4096,
                formantHopSize = 1024,
                pitchFftSize = 1024,
                pitchHopSize = 256,
            ),
        ).process(input)

        assertEquals(input.size, output.size)
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

    @Test
    fun echoDelayChangesTheOutput() {
        val input = sineWave(sampleCount = 8820, frequency = 220.0)

        val shortDelay = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                echo = 1.0,
                echoDelayMs = 50.0,
            ),
        ).process(input)
        val longDelay = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                echo = 1.0,
                echoDelayMs = 250.0,
            ),
        ).process(input)

        assertFalse(shortDelay.contentEquals(longDelay))
    }

    @Test
    fun fanSeedChangesTheNoisePattern() {
        val input = ShortArray(4410)

        val first = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                fan = 1.0,
                fanNoiseLevel = 0.5,
                fanNoiseSeed = 1,
            ),
        ).process(input)
        val second = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                fan = 1.0,
                fanNoiseLevel = 0.5,
                fanNoiseSeed = 2,
            ),
        ).process(input)

        assertFalse(first.contentEquals(second))
    }

    @Test
    fun stutterSliceChangesTheOutput() {
        val input = sineWave(sampleCount = 8820, frequency = 333.0)

        val shortSlice = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                stutter = 1.0,
                stutterSliceMs = 50.0,
            ),
        ).process(input)
        val longSlice = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                stutter = 1.0,
                stutterSliceMs = 200.0,
            ),
        ).process(input)

        assertFalse(shortSlice.contentEquals(longSlice))
    }

    @Test
    fun robotCarrierChangesTheOutput() {
        val input = sineWave(sampleCount = 8820, frequency = 220.0)

        val lowCarrier = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                robot = 1.0,
                robotCarrierHz = 40.0,
            ),
        ).process(input)
        val highCarrier = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                robot = 1.0,
                robotCarrierHz = 160.0,
            ),
        ).process(input)

        assertFalse(lowCarrier.contentEquals(highCarrier))
    }

    @Test
    fun telephoneCutoffsChangeTheOutput() {
        val input = sineWave(sampleCount = 8820, frequency = 440.0)

        val wideBand = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                telephone = 1.0,
                telephoneLowHz = 100.0,
                telephoneHighHz = 7000.0,
            ),
        ).process(input)
        val narrowBand = VoiceDistortionProcessor(
            DistortionConfig(
                pitchSemitones = 0.0,
                formantSemitones = 0.0,
                telephone = 1.0,
                telephoneLowHz = 1000.0,
                telephoneHighHz = 2000.0,
            ),
        ).process(input)

        assertFalse(wideBand.contentEquals(narrowBand))
    }

    private fun sineWave(
        sampleCount: Int,
        frequency: Double,
        amplitude: Double = 20_000.0,
    ): ShortArray = ShortArray(sampleCount) { index ->
        (sin(2.0 * PI * frequency * index / SAMPLE_RATE) * amplitude).toInt().toShort()
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
