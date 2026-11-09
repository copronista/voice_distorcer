package com.voicedistorcer.settings

import com.voicedistorcer.processing.DistortionConfig
import kotlin.math.abs

enum class VoicePreset {
    DEFAULT,
    MALE,
    FEMALE,
    CUSTOM,
}

data class VoiceSettings(
    val preset: VoicePreset = VoicePreset.DEFAULT,
    val toneSemitones: Double = DEFAULT_TONE,
    val formantSemitones: Double = DEFAULT_FORMANT,
    val speed: Double = DEFAULT_SPEED,
) {
    fun toDistortionConfig(): DistortionConfig {
        val safeFormant = if (abs(formantSemitones) < MIN_FORMANT) {
            DEFAULT_FORMANT
        } else {
            formantSemitones.coerceIn(MIN_TONE, MAX_TONE)
        }
        return DistortionConfig(
            pitchSemitones = toneSemitones.coerceIn(MIN_TONE, MAX_TONE),
            formantSemitones = safeFormant,
            speed = speed.coerceIn(MIN_SPEED, MAX_SPEED),
        )
    }

    fun withTone(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        toneSemitones = value.coerceIn(MIN_TONE, MAX_TONE),
    )

    fun withSpeed(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        speed = value.coerceIn(MIN_SPEED, MAX_SPEED),
    )

    companion object {
        const val MIN_TONE = -12.0
        const val MAX_TONE = 12.0
        const val MIN_SPEED = 0.5
        const val MAX_SPEED = 1.5

        const val DEFAULT_TONE = -4.0
        const val DEFAULT_FORMANT = 2.0
        const val DEFAULT_SPEED = 1.0

        private const val MIN_FORMANT = 0.01

        fun forPreset(preset: VoicePreset): VoiceSettings = when (preset) {
            VoicePreset.DEFAULT -> VoiceSettings(
                preset = VoicePreset.DEFAULT,
                toneSemitones = DEFAULT_TONE,
                formantSemitones = DEFAULT_FORMANT,
                speed = DEFAULT_SPEED,
            )
            VoicePreset.MALE -> VoiceSettings(
                preset = VoicePreset.MALE,
                toneSemitones = -5.0,
                formantSemitones = 1.0,
                speed = 0.95,
            )
            VoicePreset.FEMALE -> VoiceSettings(
                preset = VoicePreset.FEMALE,
                toneSemitones = 5.0,
                formantSemitones = -1.0,
                speed = 1.05,
            )
            VoicePreset.CUSTOM -> VoiceSettings(
                preset = VoicePreset.CUSTOM,
                toneSemitones = DEFAULT_TONE,
                formantSemitones = DEFAULT_FORMANT,
                speed = DEFAULT_SPEED,
            )
        }
    }
}
