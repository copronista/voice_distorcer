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
    val grit: Double = DEFAULT_GRIT,
    val robot: Double = DEFAULT_ROBOT,
) {
    fun toDistortionConfig(): DistortionConfig {
        val safeTone = toneSemitones.coerceIn(MIN_TONE, MAX_TONE)
        val safeFormant = sanitizeFormant(safeTone, formantSemitones)
        return DistortionConfig(
            pitchSemitones = safeTone,
            formantSemitones = safeFormant,
            speed = speed.coerceIn(MIN_SPEED, MAX_SPEED),
            grit = grit.coerceIn(MIN_EFFECT, MAX_EFFECT),
            robot = robot.coerceIn(MIN_EFFECT, MAX_EFFECT),
        )
    }

    fun withTone(value: Double): VoiceSettings {
        val safeTone = value.coerceIn(MIN_TONE, MAX_TONE)
        return copy(
            preset = VoicePreset.CUSTOM,
            toneSemitones = safeTone,
            formantSemitones = sanitizeFormant(safeTone, formantSemitones),
        )
    }

    fun withFormant(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        formantSemitones = sanitizeFormant(toneSemitones, value),
    )

    fun withSpeed(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        speed = value.coerceIn(MIN_SPEED, MAX_SPEED),
    )

    fun withGrit(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        grit = value.coerceIn(MIN_EFFECT, MAX_EFFECT),
    )

    fun withRobot(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        robot = value.coerceIn(MIN_EFFECT, MAX_EFFECT),
    )

    companion object {
        const val MIN_TONE = -12.0
        const val MAX_TONE = 12.0
        const val MIN_SPEED = 0.5
        const val MAX_SPEED = 1.5
        const val MIN_EFFECT = 0.0
        const val MAX_EFFECT = 1.0

        const val DEFAULT_TONE = -4.0
        const val DEFAULT_FORMANT = 2.0
        const val DEFAULT_SPEED = 1.0
        const val DEFAULT_GRIT = 0.0
        const val DEFAULT_ROBOT = 0.0

        private const val MIN_FORMANT = 1.0
        private const val MIN_COMBINED_SHIFT = 2.0

        fun sanitizeFormant(toneSemitones: Double, formantSemitones: Double): Double {
            val safeTone = toneSemitones.coerceIn(MIN_TONE, MAX_TONE)
            val safeFormant = formantSemitones.coerceIn(MIN_TONE, MAX_TONE)
            if (abs(safeTone) + abs(safeFormant) >= MIN_COMBINED_SHIFT) {
                return safeFormant
            }

            val sign = if (safeFormant < 0.0) -1.0 else 1.0
            val requiredFormant = (MIN_COMBINED_SHIFT - abs(safeTone))
                .coerceAtLeast(MIN_FORMANT)
            return sign * requiredFormant
        }

        fun forPreset(preset: VoicePreset): VoiceSettings = when (preset) {
            VoicePreset.DEFAULT -> VoiceSettings(
                preset = VoicePreset.DEFAULT,
                toneSemitones = DEFAULT_TONE,
                formantSemitones = DEFAULT_FORMANT,
                speed = DEFAULT_SPEED,
                grit = DEFAULT_GRIT,
                robot = DEFAULT_ROBOT,
            )
            VoicePreset.MALE -> VoiceSettings(
                preset = VoicePreset.MALE,
                toneSemitones = -5.0,
                formantSemitones = 1.0,
                speed = 0.95,
                grit = DEFAULT_GRIT,
                robot = DEFAULT_ROBOT,
            )
            VoicePreset.FEMALE -> VoiceSettings(
                preset = VoicePreset.FEMALE,
                toneSemitones = 5.0,
                formantSemitones = -1.0,
                speed = 1.05,
                grit = DEFAULT_GRIT,
                robot = DEFAULT_ROBOT,
            )
            VoicePreset.CUSTOM -> VoiceSettings(
                preset = VoicePreset.CUSTOM,
                toneSemitones = DEFAULT_TONE,
                formantSemitones = DEFAULT_FORMANT,
                speed = DEFAULT_SPEED,
                grit = DEFAULT_GRIT,
                robot = DEFAULT_ROBOT,
            )
        }
    }
}
