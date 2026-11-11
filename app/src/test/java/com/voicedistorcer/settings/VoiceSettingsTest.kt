package com.voicedistorcer.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceSettingsTest {
    @Test
    fun defaultPresetMatchesTheDefaultSettings() {
        val settings = VoiceSettings.forPreset(VoicePreset.DEFAULT)

        assertEquals(VoicePreset.DEFAULT, settings.preset)
        assertEquals(-4.0, settings.toneSemitones, 0.0)
        assertEquals(2.0, settings.formantSemitones, 0.0)
        assertEquals(1.0, settings.speed, 0.0)
        assertEquals(0.0, settings.grit, 0.0)
        assertEquals(0.0, settings.robot, 0.0)
        assertEquals(0.0, settings.echo, 0.0)
        assertEquals(0.0, settings.fan, 0.0)
        assertEquals(0.0, settings.stutter, 0.0)
        assertFalse(settings.reverse)
        assertEquals(0.0, settings.alien, 0.0)
        assertEquals(0.0, settings.drunk, 0.0)
        assertEquals(0.0, settings.telephone, 0.0)
        assertEquals(0.0, settings.megaphone, 0.0)
        assertEquals(0.0, settings.bee, 0.0)
        assertEquals(0.0, settings.highPassHz, 0.0)
        assertEquals(-80.0, settings.noiseGateDb, 0.0)
        assertEquals(0.0, settings.compressor, 0.0)
        assertEquals(0.0, settings.outputGainDb, 0.0)
        assertEquals(1.0, settings.formantMix, 0.0)
        assertEquals(0.5, settings.spectralSmoothing, 0.0)
        assertEquals(4.0, settings.formantGainLimit, 0.0)
        assertEquals(2048, settings.formantFftSize)
        assertEquals(4, settings.formantOverlap)
        assertEquals(2048, settings.pitchFftSize)
        assertEquals(4, settings.pitchOverlap)
    }

    @Test
    fun malePresetLowersToneAndSlowsDown() {
        val settings = VoiceSettings.forPreset(VoicePreset.MALE)

        assertEquals(VoicePreset.MALE, settings.preset)
        assertTrue(settings.toneSemitones < 0.0)
        assertTrue(settings.speed < 1.0)
        assertTrue(settings.formantSemitones > 0.0)
    }

    @Test
    fun femalePresetRaisesPitchAndFormantsWithoutChangingTempo() {
        val settings = VoiceSettings.forPreset(VoicePreset.FEMALE)

        assertEquals(VoicePreset.FEMALE, settings.preset)
        assertEquals(5.0, settings.toneSemitones, 0.0)
        assertEquals(-2.0, settings.formantSemitones, 0.0)
        assertEquals(1.0, settings.speed, 0.0)
        assertEquals(3.0, settings.toneSemitones + settings.formantSemitones, 0.0)
    }

    @Test
    fun funPresetsSetTheirSignatureEffects() {
        val chipmunk = VoiceSettings.forPreset(VoicePreset.CHIPMUNK)
        assertEquals(12.0, chipmunk.toneSemitones, 0.0)
        assertEquals(6.0, chipmunk.formantSemitones, 0.0)
        assertEquals(1.1, chipmunk.speed, 0.0)

        assertEquals(0.35, VoiceSettings.forPreset(VoicePreset.MONSTER).grit, 0.0)
        assertTrue(VoiceSettings.forPreset(VoicePreset.KID).toneSemitones > 0.0)
        assertTrue(VoiceSettings.forPreset(VoicePreset.OLD_MAN).drunk > 0.0)
        assertTrue(VoiceSettings.forPreset(VoicePreset.STUTTER).stutter > 0.0)
        assertTrue(VoiceSettings.forPreset(VoicePreset.REVERSE).reverse)
        assertTrue(VoiceSettings.forPreset(VoicePreset.ALIEN).alien > 0.0)
        assertTrue(VoiceSettings.forPreset(VoicePreset.DRUNK).drunk > 0.0)
        assertTrue(VoiceSettings.forPreset(VoicePreset.GHOST).echo > 0.0)
        assertTrue(VoiceSettings.forPreset(VoicePreset.GHOST).fan > 0.0)
        assertTrue(VoiceSettings.forPreset(VoicePreset.TELEPHONE).telephone > 0.0)
        assertTrue(VoiceSettings.forPreset(VoicePreset.MEGAPHONE).megaphone > 0.0)
        assertTrue(VoiceSettings.forPreset(VoicePreset.BEE).bee > 0.0)
    }

    @Test
    fun toneChangeSelectsCustomPresetAndClampsTheValue() {
        val settings = VoiceSettings.forPreset(VoicePreset.MALE).withTone(99.0)

        assertEquals(VoicePreset.CUSTOM, settings.preset)
        assertEquals(VoiceSettings.MAX_TONE, settings.toneSemitones, 0.0)
    }

    @Test
    fun formantChangeSelectsCustomPresetAndClampsTheValue() {
        val settings = VoiceSettings.forPreset(VoicePreset.MALE).withFormant(-99.0)

        assertEquals(VoicePreset.CUSTOM, settings.preset)
        assertEquals(VoiceSettings.MIN_TONE, settings.formantSemitones, 0.0)
    }

    @Test
    fun speedChangeSelectsCustomPresetAndClampsTheValue() {
        val settings = VoiceSettings.forPreset(VoicePreset.FEMALE).withSpeed(0.1)

        assertEquals(VoicePreset.CUSTOM, settings.preset)
        assertEquals(VoiceSettings.MIN_SPEED, settings.speed, 0.0)
    }

    @Test
    fun characterEffectsSelectCustomPresetAndClampTheValues() {
        val tooMuch = VoiceSettings.forPreset(VoicePreset.DEFAULT)
            .withGrit(9.0)
            .withRobot(-1.0)
            .withEcho(9.0)
            .withFan(-1.0)
        val tooLittle = VoiceSettings.forPreset(VoicePreset.DEFAULT)
            .withGrit(-1.0)
            .withRobot(9.0)
            .withEcho(-1.0)
            .withFan(9.0)

        assertEquals(VoicePreset.CUSTOM, tooMuch.preset)
        assertEquals(VoiceSettings.MAX_EFFECT, tooMuch.grit, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, tooMuch.robot, 0.0)
        assertEquals(VoiceSettings.MAX_EFFECT, tooMuch.echo, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, tooMuch.fan, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, tooLittle.grit, 0.0)
        assertEquals(VoiceSettings.MAX_EFFECT, tooLittle.robot, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, tooLittle.echo, 0.0)
        assertEquals(VoiceSettings.MAX_EFFECT, tooLittle.fan, 0.0)
    }

    @Test
    fun funEffectsSelectCustomPresetAndClampTheValues() {
        val settings = VoiceSettings.forPreset(VoicePreset.DEFAULT)
            .withStutter(9.0)
            .withReverse(true)
            .withAlien(-1.0)
            .withDrunk(9.0)
            .withTelephone(-1.0)
            .withMegaphone(9.0)
            .withBee(-1.0)

        assertEquals(VoicePreset.CUSTOM, settings.preset)
        assertEquals(VoiceSettings.MAX_EFFECT, settings.stutter, 0.0)
        assertTrue(settings.reverse)
        assertEquals(VoiceSettings.MIN_EFFECT, settings.alien, 0.0)
        assertEquals(VoiceSettings.MAX_EFFECT, settings.drunk, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, settings.telephone, 0.0)
        assertEquals(VoiceSettings.MAX_EFFECT, settings.megaphone, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, settings.bee, 0.0)
    }

    @Test
    fun configAlwaysKeepsAnonymizationEnabled() {
        val settings = VoiceSettings(
            preset = VoicePreset.CUSTOM,
            toneSemitones = 0.0,
            formantSemitones = 0.0,
            speed = 1.0,
        )

        val config = settings.toDistortionConfig()

        assertEquals(0.0, config.pitchSemitones, 0.0)
        assertNotEquals(0.0, config.formantSemitones, 0.0)
    }

    @Test
    fun configRaisesFormantWhenCombinedShiftIsTooSmall() {
        val settings = VoiceSettings(
            preset = VoicePreset.CUSTOM,
            toneSemitones = 1.0,
            formantSemitones = 0.0,
            speed = 1.0,
        )

        val config = settings.toDistortionConfig()

        assertEquals(1.0, config.formantSemitones, 0.0)
    }

    @Test
    fun toneChangeRaisesFormantToKeepAnonymizationEnabled() {
        val settings = VoiceSettings.forPreset(VoicePreset.DEFAULT)
            .withFormant(0.0)
            .withTone(0.0)

        assertEquals(2.0, settings.formantSemitones, 0.0)
    }

    @Test
    fun formantChangeRaisesZeroToTheMinimumWhenPitchIsSmall() {
        val settings = VoiceSettings(
            preset = VoicePreset.CUSTOM,
            toneSemitones = 0.0,
            formantSemitones = 0.0,
            speed = 1.0,
        ).withFormant(0.0)

        assertEquals(2.0, settings.formantSemitones, 0.0)
    }

    @Test
    fun configAllowsZeroFormantWhenPitchShiftIsActive() {
        val settings = VoiceSettings(
            preset = VoicePreset.CUSTOM,
            toneSemitones = -4.0,
            formantSemitones = 0.0,
            speed = 1.0,
        )

        val config = settings.toDistortionConfig()

        assertEquals(0.0, config.formantSemitones, 0.0)
    }

    @Test
    fun configMapsCustomDspParameters() {
        val settings = VoiceSettings(
            preset = VoicePreset.CUSTOM,
            toneSemitones = -4.0,
            formantSemitones = 2.0,
            formantFftSize = 4096,
            formantOverlap = 2,
            pitchFftSize = 1024,
            pitchOverlap = 8,
        )

        val config = settings.toDistortionConfig()

        assertEquals(4096, config.formantFftSize)
        assertEquals(2048, config.formantHopSize)
        assertEquals(1024, config.pitchFftSize)
        assertEquals(128, config.pitchHopSize)
    }

    @Test
    fun advancedChangesSelectCustomPreset() {
        val settings = VoiceSettings.forPreset(VoicePreset.DEFAULT)
            .withHighPass(100.0)
            .withNoiseGate(-40.0)
            .withCompressor(0.5)
            .withOutputGain(-3.0)
            .withFormantMix(0.5)
            .withSpectralSmoothing(0.8)
            .withFormantGainLimit(2.0)
            .withFormantFftSize(4096)
            .withFormantOverlap(8)
            .withPitchFftSize(1024)
            .withPitchOverlap(8)

        assertEquals(VoicePreset.CUSTOM, settings.preset)
        assertEquals(100.0, settings.highPassHz, 0.0)
        assertEquals(-40.0, settings.noiseGateDb, 0.0)
        assertEquals(0.5, settings.compressor, 0.0)
        assertEquals(-3.0, settings.outputGainDb, 0.0)
        assertEquals(0.5, settings.formantMix, 0.0)
        assertEquals(0.8, settings.spectralSmoothing, 0.0)
        assertEquals(2.0, settings.formantGainLimit, 0.0)
        assertEquals(4096, settings.formantFftSize)
        assertEquals(8, settings.formantOverlap)
        assertEquals(1024, settings.pitchFftSize)
        assertEquals(8, settings.pitchOverlap)
    }

    @Test
    fun configClampsAllValuesToSupportedRanges() {
        val settings = VoiceSettings(
            preset = VoicePreset.CUSTOM,
            toneSemitones = 50.0,
            formantSemitones = 50.0,
            speed = 9.0,
            grit = 9.0,
            robot = -1.0,
            echo = 9.0,
            fan = -1.0,
            stutter = 9.0,
            reverse = true,
            alien = -1.0,
            drunk = 9.0,
            telephone = -1.0,
            megaphone = 9.0,
            bee = -1.0,
            highPassHz = 999.0,
            noiseGateDb = -999.0,
            compressor = 9.0,
            outputGainDb = 99.0,
            formantMix = 9.0,
            spectralSmoothing = -1.0,
            formantGainLimit = 99.0,
            formantFftSize = 9999,
            formantOverlap = 99,
            pitchFftSize = 1,
            pitchOverlap = 1,
        )

        val config = settings.toDistortionConfig()

        assertEquals(VoiceSettings.MAX_TONE, config.pitchSemitones, 0.0)
        assertEquals(VoiceSettings.MAX_TONE, config.formantSemitones, 0.0)
        assertEquals(VoiceSettings.MAX_SPEED, config.speed, 0.0)
        assertEquals(VoiceSettings.MAX_EFFECT, config.grit, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, config.robot, 0.0)
        assertEquals(VoiceSettings.MAX_EFFECT, config.echo, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, config.fan, 0.0)
        assertEquals(VoiceSettings.MAX_EFFECT, config.stutter, 0.0)
        assertTrue(config.reverse)
        assertEquals(VoiceSettings.MIN_EFFECT, config.alien, 0.0)
        assertEquals(VoiceSettings.MAX_EFFECT, config.drunk, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, config.telephone, 0.0)
        assertEquals(VoiceSettings.MAX_EFFECT, config.megaphone, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, config.bee, 0.0)
        assertEquals(VoiceSettings.MAX_HIGH_PASS_HZ, config.highPassHz, 0.0)
        assertEquals(VoiceSettings.MIN_NOISE_GATE_DB, config.noiseGateDb, 0.0)
        assertEquals(VoiceSettings.MAX_COMPRESSOR, config.compressor, 0.0)
        assertEquals(VoiceSettings.MAX_OUTPUT_GAIN_DB, config.outputGainDb, 0.0)
        assertEquals(VoiceSettings.MAX_FORMANT_MIX, config.formantMix, 0.0)
        assertEquals(VoiceSettings.MIN_SPECTRAL_SMOOTHING, config.spectralSmoothing, 0.0)
        assertEquals(VoiceSettings.MAX_FORMANT_GAIN_LIMIT, config.formantGainLimit, 0.0)
        assertEquals(4096, config.formantFftSize)
        assertEquals(512, config.formantHopSize)
        assertEquals(1024, config.pitchFftSize)
        assertEquals(256, config.pitchHopSize)
    }
}
