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
        assertEquals(4.0, settings.gritDrive, 0.0)
        assertEquals(180.0, settings.echoDelayMs, 0.0)
        assertEquals(0.7, settings.echoWet, 0.0)
        assertEquals(0.45, settings.echoFeedback, 0.0)
        assertEquals(1200.0, settings.fanCutoffHz, 0.0)
        assertEquals(0.3, settings.fanModulationHz, 0.0)
        assertEquals(0.18, settings.fanNoiseLevel, 0.0)
        assertEquals(0x12345678, settings.fanNoiseSeed)
        assertEquals(120.0, settings.stutterSliceMs, 0.0)
        assertEquals(0x6D2B79F5, settings.stutterSeed)
        assertEquals(220.0, settings.alienCarrierHz, 0.0)
        assertEquals(0.6, settings.alienWobbleHz, 0.0)
        assertEquals(5.0, settings.alienTremoloHz, 0.0)
        assertEquals(15.0, settings.drunkMaxDelayMs, 0.0)
        assertEquals(0.8, settings.drunkWobbleHz, 0.0)
        assertEquals(4.5, settings.drunkTremoloHz, 0.0)
        assertEquals(300.0, settings.telephoneLowHz, 0.0)
        assertEquals(3400.0, settings.telephoneHighHz, 0.0)
        assertEquals(2.0, settings.telephoneDrive, 0.0)
        assertEquals(500.0, settings.megaphoneLowHz, 0.0)
        assertEquals(3000.0, settings.megaphoneHighHz, 0.0)
        assertEquals(4.0, settings.megaphoneDrive, 0.0)
        assertEquals(35.0, settings.beeBuzzHz, 0.0)
        assertEquals(1.17, settings.beeBuzzRatio, 0.0)
        assertEquals(0.7, settings.beeFlutterHz, 0.0)
        assertEquals(7.0, settings.compressorMaxRatio, 0.0)
        assertEquals(0.25, settings.compressorThreshold, 0.0)
        assertEquals(5.0, settings.compressorAttackMs, 0.0)
        assertEquals(100.0, settings.compressorReleaseMs, 0.0)
        assertEquals(5.0, settings.gateAttackMs, 0.0)
        assertEquals(100.0, settings.gateReleaseMs, 0.0)
        assertEquals(80.0, settings.robotCarrierHz, 0.0)
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
    fun effectParameterChangesSelectCustomPresetAndClampTheValues() {
        val settings = VoiceSettings.forPreset(VoicePreset.DEFAULT)
            .withGritDrive(99.0)
            .withEchoDelay(0.0)
            .withEchoWet(9.0)
            .withEchoFeedback(-1.0)
            .withFanCutoff(0.0)
            .withFanModulation(99.0)
            .withFanNoiseLevel(-1.0)
            .withFanNoiseSeed(42)
            .withStutterSlice(0.0)
            .withStutterSeed(43)
            .withAlienCarrier(0.0)
            .withAlienWobble(99.0)
            .withAlienTremolo(-1.0)
            .withDrunkMaxDelay(99.0)
            .withDrunkWobble(-1.0)
            .withDrunkTremolo(99.0)
            .withTelephoneLow(0.0)
            .withTelephoneHigh(99_000.0)
            .withTelephoneDrive(0.0)
            .withMegaphoneLow(0.0)
            .withMegaphoneHigh(99_000.0)
            .withMegaphoneDrive(99.0)
            .withBeeBuzz(0.0)
            .withBeeBuzzRatio(9.0)
            .withBeeFlutter(-1.0)
            .withCompressorMaxRatio(0.0)
            .withCompressorThreshold(0.0)
            .withCompressorAttack(0.0)
            .withCompressorRelease(0.0)
            .withGateAttack(0.0)
            .withGateRelease(0.0)
            .withRobotCarrier(0.0)

        assertEquals(VoicePreset.CUSTOM, settings.preset)
        assertEquals(VoiceSettings.MAX_GRIT_DRIVE, settings.gritDrive, 0.0)
        assertEquals(VoiceSettings.MIN_ECHO_DELAY_MS, settings.echoDelayMs, 0.0)
        assertEquals(VoiceSettings.MAX_ECHO_WET, settings.echoWet, 0.0)
        assertEquals(VoiceSettings.MIN_ECHO_FEEDBACK, settings.echoFeedback, 0.0)
        assertEquals(VoiceSettings.MIN_FAN_CUTOFF_HZ, settings.fanCutoffHz, 0.0)
        assertEquals(VoiceSettings.MAX_FAN_MODULATION_HZ, settings.fanModulationHz, 0.0)
        assertEquals(VoiceSettings.MIN_FAN_NOISE_LEVEL, settings.fanNoiseLevel, 0.0)
        assertEquals(42, settings.fanNoiseSeed)
        assertEquals(VoiceSettings.MIN_STUTTER_SLICE_MS, settings.stutterSliceMs, 0.0)
        assertEquals(43, settings.stutterSeed)
        assertEquals(VoiceSettings.MIN_ALIEN_CARRIER_HZ, settings.alienCarrierHz, 0.0)
        assertEquals(VoiceSettings.MAX_ALIEN_WOBBLE_HZ, settings.alienWobbleHz, 0.0)
        assertEquals(VoiceSettings.MIN_ALIEN_TREMOLO_HZ, settings.alienTremoloHz, 0.0)
        assertEquals(VoiceSettings.MAX_DRUNK_MAX_DELAY_MS, settings.drunkMaxDelayMs, 0.0)
        assertEquals(VoiceSettings.MIN_DRUNK_WOBBLE_HZ, settings.drunkWobbleHz, 0.0)
        assertEquals(VoiceSettings.MAX_DRUNK_TREMOLO_HZ, settings.drunkTremoloHz, 0.0)
        assertEquals(VoiceSettings.MIN_TELEPHONE_LOW_HZ, settings.telephoneLowHz, 0.0)
        assertEquals(VoiceSettings.MAX_TELEPHONE_HIGH_HZ, settings.telephoneHighHz, 0.0)
        assertEquals(VoiceSettings.MIN_TELEPHONE_DRIVE, settings.telephoneDrive, 0.0)
        assertEquals(VoiceSettings.MIN_MEGAPHONE_LOW_HZ, settings.megaphoneLowHz, 0.0)
        assertEquals(VoiceSettings.MAX_MEGAPHONE_HIGH_HZ, settings.megaphoneHighHz, 0.0)
        assertEquals(VoiceSettings.MAX_MEGAPHONE_DRIVE, settings.megaphoneDrive, 0.0)
        assertEquals(VoiceSettings.MIN_BEE_BUZZ_HZ, settings.beeBuzzHz, 0.0)
        assertEquals(VoiceSettings.MAX_BEE_BUZZ_RATIO, settings.beeBuzzRatio, 0.0)
        assertEquals(VoiceSettings.MIN_BEE_FLUTTER_HZ, settings.beeFlutterHz, 0.0)
        assertEquals(VoiceSettings.MIN_COMPRESSOR_MAX_RATIO, settings.compressorMaxRatio, 0.0)
        assertEquals(VoiceSettings.MIN_COMPRESSOR_THRESHOLD, settings.compressorThreshold, 0.0)
        assertEquals(VoiceSettings.MIN_COMPRESSOR_ATTACK_MS, settings.compressorAttackMs, 0.0)
        assertEquals(VoiceSettings.MIN_COMPRESSOR_RELEASE_MS, settings.compressorReleaseMs, 0.0)
        assertEquals(VoiceSettings.MIN_GATE_ATTACK_MS, settings.gateAttackMs, 0.0)
        assertEquals(VoiceSettings.MIN_GATE_RELEASE_MS, settings.gateReleaseMs, 0.0)
        assertEquals(VoiceSettings.MIN_ROBOT_CARRIER_HZ, settings.robotCarrierHz, 0.0)
    }

    @Test
    fun configMapsEffectParameters() {
        val settings = VoiceSettings(
            preset = VoicePreset.CUSTOM,
            gritDrive = 6.0,
            echoDelayMs = 250.0,
            echoWet = 0.4,
            echoFeedback = 0.2,
            fanCutoffHz = 900.0,
            fanModulationHz = 1.2,
            fanNoiseLevel = 0.3,
            fanNoiseSeed = 123,
            stutterSliceMs = 90.0,
            stutterSeed = 456,
            alienCarrierHz = 180.0,
            alienWobbleHz = 1.1,
            alienTremoloHz = 6.0,
            drunkMaxDelayMs = 20.0,
            drunkWobbleHz = 1.5,
            drunkTremoloHz = 3.0,
            telephoneLowHz = 400.0,
            telephoneHighHz = 3200.0,
            telephoneDrive = 3.0,
            megaphoneLowHz = 600.0,
            megaphoneHighHz = 2800.0,
            megaphoneDrive = 5.0,
            beeBuzzHz = 45.0,
            beeBuzzRatio = 1.3,
            beeFlutterHz = 1.4,
            compressorMaxRatio = 9.0,
            compressorThreshold = 0.3,
            compressorAttackMs = 8.0,
            compressorReleaseMs = 150.0,
            gateAttackMs = 7.0,
            gateReleaseMs = 120.0,
            robotCarrierHz = 110.0,
        )

        val config = settings.toDistortionConfig()

        assertEquals(6.0, config.gritDrive, 0.0)
        assertEquals(250.0, config.echoDelayMs, 0.0)
        assertEquals(0.4, config.echoWet, 0.0)
        assertEquals(0.2, config.echoFeedback, 0.0)
        assertEquals(900.0, config.fanCutoffHz, 0.0)
        assertEquals(1.2, config.fanModulationHz, 0.0)
        assertEquals(0.3, config.fanNoiseLevel, 0.0)
        assertEquals(123, config.fanNoiseSeed)
        assertEquals(90.0, config.stutterSliceMs, 0.0)
        assertEquals(456, config.stutterSeed)
        assertEquals(180.0, config.alienCarrierHz, 0.0)
        assertEquals(1.1, config.alienWobbleHz, 0.0)
        assertEquals(6.0, config.alienTremoloHz, 0.0)
        assertEquals(20.0, config.drunkMaxDelayMs, 0.0)
        assertEquals(1.5, config.drunkWobbleHz, 0.0)
        assertEquals(3.0, config.drunkTremoloHz, 0.0)
        assertEquals(400.0, config.telephoneLowHz, 0.0)
        assertEquals(3200.0, config.telephoneHighHz, 0.0)
        assertEquals(3.0, config.telephoneDrive, 0.0)
        assertEquals(600.0, config.megaphoneLowHz, 0.0)
        assertEquals(2800.0, config.megaphoneHighHz, 0.0)
        assertEquals(5.0, config.megaphoneDrive, 0.0)
        assertEquals(45.0, config.beeBuzzHz, 0.0)
        assertEquals(1.3, config.beeBuzzRatio, 0.0)
        assertEquals(1.4, config.beeFlutterHz, 0.0)
        assertEquals(9.0, config.compressorMaxRatio, 0.0)
        assertEquals(0.3, config.compressorThreshold, 0.0)
        assertEquals(8.0, config.compressorAttackMs, 0.0)
        assertEquals(150.0, config.compressorReleaseMs, 0.0)
        assertEquals(7.0, config.gateAttackMs, 0.0)
        assertEquals(120.0, config.gateReleaseMs, 0.0)
        assertEquals(110.0, config.robotCarrierHz, 0.0)
    }

    @Test
    fun configKeepsTelephoneAndMegaphoneBandsOrdered() {
        val settings = VoiceSettings(
            preset = VoicePreset.CUSTOM,
            telephoneLowHz = 1500.0,
            telephoneHighHz = 1000.0,
            megaphoneLowHz = 2000.0,
            megaphoneHighHz = 1000.0,
        )

        val config = settings.toDistortionConfig()

        assertTrue(config.telephoneHighHz > config.telephoneLowHz)
        assertTrue(config.megaphoneHighHz > config.megaphoneLowHz)
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
            gritDrive = 99.0,
            echoDelayMs = 0.0,
            echoWet = 9.0,
            echoFeedback = -1.0,
            fanCutoffHz = 0.0,
            fanModulationHz = 99.0,
            fanNoiseLevel = -1.0,
            fanNoiseSeed = 123,
            stutterSliceMs = 0.0,
            stutterSeed = 456,
            alienCarrierHz = 0.0,
            alienWobbleHz = 99.0,
            alienTremoloHz = -1.0,
            drunkMaxDelayMs = 99.0,
            drunkWobbleHz = -1.0,
            drunkTremoloHz = 99.0,
            telephoneLowHz = 0.0,
            telephoneHighHz = 99_000.0,
            telephoneDrive = 0.0,
            megaphoneLowHz = 0.0,
            megaphoneHighHz = 99_000.0,
            megaphoneDrive = 99.0,
            beeBuzzHz = 0.0,
            beeBuzzRatio = 9.0,
            beeFlutterHz = -1.0,
            compressorMaxRatio = 0.0,
            compressorThreshold = 0.0,
            compressorAttackMs = 0.0,
            compressorReleaseMs = 0.0,
            gateAttackMs = 0.0,
            gateReleaseMs = 0.0,
            robotCarrierHz = 0.0,
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
        assertEquals(VoiceSettings.MAX_GRIT_DRIVE, config.gritDrive, 0.0)
        assertEquals(VoiceSettings.MIN_ECHO_DELAY_MS, config.echoDelayMs, 0.0)
        assertEquals(VoiceSettings.MAX_ECHO_WET, config.echoWet, 0.0)
        assertEquals(VoiceSettings.MIN_ECHO_FEEDBACK, config.echoFeedback, 0.0)
        assertEquals(VoiceSettings.MIN_FAN_CUTOFF_HZ, config.fanCutoffHz, 0.0)
        assertEquals(VoiceSettings.MAX_FAN_MODULATION_HZ, config.fanModulationHz, 0.0)
        assertEquals(VoiceSettings.MIN_FAN_NOISE_LEVEL, config.fanNoiseLevel, 0.0)
        assertEquals(123, config.fanNoiseSeed)
        assertEquals(VoiceSettings.MIN_STUTTER_SLICE_MS, config.stutterSliceMs, 0.0)
        assertEquals(456, config.stutterSeed)
        assertEquals(VoiceSettings.MIN_ALIEN_CARRIER_HZ, config.alienCarrierHz, 0.0)
        assertEquals(VoiceSettings.MAX_ALIEN_WOBBLE_HZ, config.alienWobbleHz, 0.0)
        assertEquals(VoiceSettings.MIN_ALIEN_TREMOLO_HZ, config.alienTremoloHz, 0.0)
        assertEquals(VoiceSettings.MAX_DRUNK_MAX_DELAY_MS, config.drunkMaxDelayMs, 0.0)
        assertEquals(VoiceSettings.MIN_DRUNK_WOBBLE_HZ, config.drunkWobbleHz, 0.0)
        assertEquals(VoiceSettings.MAX_DRUNK_TREMOLO_HZ, config.drunkTremoloHz, 0.0)
        assertEquals(VoiceSettings.MIN_TELEPHONE_LOW_HZ, config.telephoneLowHz, 0.0)
        assertEquals(VoiceSettings.MAX_TELEPHONE_HIGH_HZ, config.telephoneHighHz, 0.0)
        assertEquals(VoiceSettings.MIN_TELEPHONE_DRIVE, config.telephoneDrive, 0.0)
        assertEquals(VoiceSettings.MIN_MEGAPHONE_LOW_HZ, config.megaphoneLowHz, 0.0)
        assertEquals(VoiceSettings.MAX_MEGAPHONE_HIGH_HZ, config.megaphoneHighHz, 0.0)
        assertEquals(VoiceSettings.MAX_MEGAPHONE_DRIVE, config.megaphoneDrive, 0.0)
        assertEquals(VoiceSettings.MIN_BEE_BUZZ_HZ, config.beeBuzzHz, 0.0)
        assertEquals(VoiceSettings.MAX_BEE_BUZZ_RATIO, config.beeBuzzRatio, 0.0)
        assertEquals(VoiceSettings.MIN_BEE_FLUTTER_HZ, config.beeFlutterHz, 0.0)
        assertEquals(VoiceSettings.MIN_COMPRESSOR_MAX_RATIO, config.compressorMaxRatio, 0.0)
        assertEquals(VoiceSettings.MIN_COMPRESSOR_THRESHOLD, config.compressorThreshold, 0.0)
        assertEquals(VoiceSettings.MIN_COMPRESSOR_ATTACK_MS, config.compressorAttackMs, 0.0)
        assertEquals(VoiceSettings.MIN_COMPRESSOR_RELEASE_MS, config.compressorReleaseMs, 0.0)
        assertEquals(VoiceSettings.MIN_GATE_ATTACK_MS, config.gateAttackMs, 0.0)
        assertEquals(VoiceSettings.MIN_GATE_RELEASE_MS, config.gateReleaseMs, 0.0)
        assertEquals(VoiceSettings.MIN_ROBOT_CARRIER_HZ, config.robotCarrierHz, 0.0)
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
