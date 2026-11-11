package com.voicedistorcer.settings

import org.junit.Assert.assertEquals
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
    fun femalePresetRaisesToneAndSpeedsUp() {
        val settings = VoiceSettings.forPreset(VoicePreset.FEMALE)

        assertEquals(VoicePreset.FEMALE, settings.preset)
        assertTrue(settings.toneSemitones > 0.0)
        assertTrue(settings.speed > 1.0)
        assertTrue(settings.formantSemitones < 0.0)
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
        val tooLittle = VoiceSettings.forPreset(VoicePreset.DEFAULT)
            .withGrit(-1.0)
            .withRobot(9.0)

        assertEquals(VoicePreset.CUSTOM, tooMuch.preset)
        assertEquals(VoiceSettings.MAX_EFFECT, tooMuch.grit, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, tooMuch.robot, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, tooLittle.grit, 0.0)
        assertEquals(VoiceSettings.MAX_EFFECT, tooLittle.robot, 0.0)
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
    fun configClampsAllValuesToSupportedRanges() {
        val settings = VoiceSettings(
            preset = VoicePreset.CUSTOM,
            toneSemitones = 50.0,
            formantSemitones = 50.0,
            speed = 9.0,
            grit = 9.0,
            robot = -1.0,
        )

        val config = settings.toDistortionConfig()

        assertEquals(VoiceSettings.MAX_TONE, config.pitchSemitones, 0.0)
        assertEquals(VoiceSettings.MAX_TONE, config.formantSemitones, 0.0)
        assertEquals(VoiceSettings.MAX_SPEED, config.speed, 0.0)
        assertEquals(VoiceSettings.MAX_EFFECT, config.grit, 0.0)
        assertEquals(VoiceSettings.MIN_EFFECT, config.robot, 0.0)
    }
}
