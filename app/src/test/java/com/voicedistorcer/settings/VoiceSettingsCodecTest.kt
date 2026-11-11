package com.voicedistorcer.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceSettingsCodecTest {
    @Test
    fun encodeAndDecodePreserveAllSettings() {
        val settings = VoiceSettings(
            preset = VoicePreset.CUSTOM,
            toneSemitones = 7.0,
            formantSemitones = -3.0,
            speed = 1.2,
            grit = 0.4,
            robot = 0.3,
            echo = 0.6,
            fan = 0.2,
            stutter = 0.5,
            reverse = true,
            alien = 0.4,
            drunk = 0.2,
            telephone = 0.8,
            megaphone = 0.7,
            bee = 0.9,
            gritDrive = 6.0,
            echoDelayMs = 250.0,
            echoWet = 0.8,
            echoFeedback = 0.5,
            fanCutoffHz = 2000.0,
            fanModulationHz = 1.5,
            fanNoiseLevel = 0.3,
            fanNoiseSeed = 42,
            stutterSliceMs = 90.0,
            stutterSeed = 43,
            alienCarrierHz = 300.0,
            alienWobbleHz = 2.0,
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
            beeBuzzHz = 40.0,
            beeBuzzRatio = 1.2,
            beeFlutterHz = 1.0,
            compressorMaxRatio = 9.0,
            compressorThreshold = 0.3,
            compressorAttackMs = 10.0,
            compressorReleaseMs = 150.0,
            gateAttackMs = 6.0,
            gateReleaseMs = 120.0,
            robotCarrierHz = 100.0,
            highPassHz = 100.0,
            noiseGateDb = -40.0,
            compressor = 0.5,
            outputGainDb = -3.0,
            formantMix = 0.6,
            spectralSmoothing = 0.7,
            formantGainLimit = 2.5,
            formantFftSize = 4096,
            formantOverlap = 2,
            pitchFftSize = 1024,
            pitchOverlap = 8,
        )

        val encoded = VoiceSettingsCodec.encode(settings)
        val decoded = VoiceSettingsCodec.decode(encoded)

        assertEquals(settings.sanitized(), decoded)
        assertTrue(encoded.contains("format=${VoiceSettingsCodec.FORMAT}"))
    }

    @Test
    fun decodeRejectsUnknownFormat() {
        val encoded = """
            format=another-app
            version=1
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            VoiceSettingsCodec.decode(encoded)
        }
    }

    @Test
    fun decodeRejectsFutureVersion() {
        val encoded = """
            format=${VoiceSettingsCodec.FORMAT}
            version=99
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            VoiceSettingsCodec.decode(encoded)
        }
    }

    @Test
    fun decodeSanitizesOutOfRangeValues() {
        val encoded = """
            format=${VoiceSettingsCodec.FORMAT}
            version=1
            preset=CUSTOM
            toneSemitones=99
            formantSemitones=0
            speed=9
            telephoneLowHz=4000
            telephoneHighHz=100
        """.trimIndent()

        val decoded = VoiceSettingsCodec.decode(encoded)

        assertEquals(VoiceSettings.MAX_TONE, decoded.toneSemitones, 0.0)
        assertEquals(VoiceSettings.MAX_SPEED, decoded.speed, 0.0)
        assertTrue(decoded.telephoneHighHz > decoded.telephoneLowHz)
    }

    @Test
    fun decodeUsesPresetDefaultsForMissingValues() {
        val encoded = """
            format=${VoiceSettingsCodec.FORMAT}
            version=1
            preset=MONSTER
        """.trimIndent()

        val decoded = VoiceSettingsCodec.decode(encoded)
        val fallback = VoiceSettings.forPreset(VoicePreset.MONSTER)

        assertEquals(VoicePreset.MONSTER, decoded.preset)
        assertEquals(fallback.grit, decoded.grit, 0.0)
        assertEquals(fallback.speed, decoded.speed, 0.0)
    }
}
