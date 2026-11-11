package com.voicedistorcer.settings

import com.voicedistorcer.processing.DistortionConfig
import kotlin.math.abs

enum class VoicePreset {
    DEFAULT,
    MALE,
    FEMALE,
    CHIPMUNK,
    MONSTER,
    KID,
    OLD_MAN,
    STUTTER,
    REVERSE,
    ALIEN,
    DRUNK,
    GHOST,
    TELEPHONE,
    MEGAPHONE,
    BEE,
    CUSTOM,
}

data class VoiceSettings(
    val preset: VoicePreset = VoicePreset.DEFAULT,
    val toneSemitones: Double = DEFAULT_TONE,
    val formantSemitones: Double = DEFAULT_FORMANT,
    val speed: Double = DEFAULT_SPEED,
    val grit: Double = DEFAULT_GRIT,
    val robot: Double = DEFAULT_ROBOT,
    val echo: Double = DEFAULT_ECHO,
    val fan: Double = DEFAULT_FAN,
    val stutter: Double = DEFAULT_STUTTER,
    val reverse: Boolean = DEFAULT_REVERSE,
    val alien: Double = DEFAULT_ALIEN,
    val drunk: Double = DEFAULT_DRUNK,
    val telephone: Double = DEFAULT_TELEPHONE,
    val megaphone: Double = DEFAULT_MEGAPHONE,
    val bee: Double = DEFAULT_BEE,
    val highPassHz: Double = DEFAULT_HIGH_PASS_HZ,
    val noiseGateDb: Double = DEFAULT_NOISE_GATE_DB,
    val compressor: Double = DEFAULT_COMPRESSOR,
    val outputGainDb: Double = DEFAULT_OUTPUT_GAIN_DB,
    val formantMix: Double = DEFAULT_FORMANT_MIX,
    val spectralSmoothing: Double = DEFAULT_SPECTRAL_SMOOTHING,
    val formantGainLimit: Double = DEFAULT_FORMANT_GAIN_LIMIT,
    val formantFftSize: Int = DEFAULT_FORMANT_FFT_SIZE,
    val formantOverlap: Int = DEFAULT_FORMANT_OVERLAP,
    val pitchFftSize: Int = DEFAULT_PITCH_FFT_SIZE,
    val pitchOverlap: Int = DEFAULT_PITCH_OVERLAP,
) {
    fun toDistortionConfig(): DistortionConfig {
        val safeTone = toneSemitones.coerceIn(MIN_TONE, MAX_TONE)
        val safeFormant = sanitizeFormant(safeTone, formantSemitones)
        val formantFft = normalizeFftSize(formantFftSize)
        val formantHop = (formantFft / normalizeFormantOverlap(formantOverlap))
            .coerceIn(1, formantFft - 1)
        val pitchFft = normalizeFftSize(pitchFftSize)
        val pitchHop = (pitchFft / normalizePitchOverlap(pitchOverlap))
            .coerceIn(1, pitchFft / 2 - 1)
        return DistortionConfig(
            pitchSemitones = safeTone,
            formantSemitones = safeFormant,
            speed = speed.coerceIn(MIN_SPEED, MAX_SPEED),
            grit = grit.coerceIn(MIN_EFFECT, MAX_EFFECT),
            robot = robot.coerceIn(MIN_EFFECT, MAX_EFFECT),
            echo = echo.coerceIn(MIN_EFFECT, MAX_EFFECT),
            fan = fan.coerceIn(MIN_EFFECT, MAX_EFFECT),
            stutter = stutter.coerceIn(MIN_EFFECT, MAX_EFFECT),
            reverse = reverse,
            alien = alien.coerceIn(MIN_EFFECT, MAX_EFFECT),
            drunk = drunk.coerceIn(MIN_EFFECT, MAX_EFFECT),
            telephone = telephone.coerceIn(MIN_EFFECT, MAX_EFFECT),
            megaphone = megaphone.coerceIn(MIN_EFFECT, MAX_EFFECT),
            bee = bee.coerceIn(MIN_EFFECT, MAX_EFFECT),
            highPassHz = highPassHz.coerceIn(MIN_HIGH_PASS_HZ, MAX_HIGH_PASS_HZ),
            noiseGateDb = noiseGateDb.coerceIn(MIN_NOISE_GATE_DB, MAX_NOISE_GATE_DB),
            compressor = compressor.coerceIn(MIN_COMPRESSOR, MAX_COMPRESSOR),
            outputGainDb = outputGainDb.coerceIn(MIN_OUTPUT_GAIN_DB, MAX_OUTPUT_GAIN_DB),
            formantMix = formantMix.coerceIn(MIN_FORMANT_MIX, MAX_FORMANT_MIX),
            spectralSmoothing = spectralSmoothing.coerceIn(
                MIN_SPECTRAL_SMOOTHING,
                MAX_SPECTRAL_SMOOTHING,
            ),
            formantGainLimit = formantGainLimit.coerceIn(
                MIN_FORMANT_GAIN_LIMIT,
                MAX_FORMANT_GAIN_LIMIT,
            ),
            formantFftSize = formantFft,
            formantHopSize = formantHop,
            pitchFftSize = pitchFft,
            pitchHopSize = pitchHop,
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

    fun withEcho(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        echo = value.coerceIn(MIN_EFFECT, MAX_EFFECT),
    )

    fun withFan(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        fan = value.coerceIn(MIN_EFFECT, MAX_EFFECT),
    )

    fun withStutter(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        stutter = value.coerceIn(MIN_EFFECT, MAX_EFFECT),
    )

    fun withReverse(value: Boolean): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        reverse = value,
    )

    fun withAlien(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        alien = value.coerceIn(MIN_EFFECT, MAX_EFFECT),
    )

    fun withDrunk(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        drunk = value.coerceIn(MIN_EFFECT, MAX_EFFECT),
    )

    fun withTelephone(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        telephone = value.coerceIn(MIN_EFFECT, MAX_EFFECT),
    )

    fun withMegaphone(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        megaphone = value.coerceIn(MIN_EFFECT, MAX_EFFECT),
    )

    fun withBee(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        bee = value.coerceIn(MIN_EFFECT, MAX_EFFECT),
    )

    fun withHighPass(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        highPassHz = value.coerceIn(MIN_HIGH_PASS_HZ, MAX_HIGH_PASS_HZ),
    )

    fun withNoiseGate(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        noiseGateDb = value.coerceIn(MIN_NOISE_GATE_DB, MAX_NOISE_GATE_DB),
    )

    fun withCompressor(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        compressor = value.coerceIn(MIN_COMPRESSOR, MAX_COMPRESSOR),
    )

    fun withOutputGain(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        outputGainDb = value.coerceIn(MIN_OUTPUT_GAIN_DB, MAX_OUTPUT_GAIN_DB),
    )

    fun withFormantMix(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        formantMix = value.coerceIn(MIN_FORMANT_MIX, MAX_FORMANT_MIX),
    )

    fun withSpectralSmoothing(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        spectralSmoothing = value.coerceIn(
            MIN_SPECTRAL_SMOOTHING,
            MAX_SPECTRAL_SMOOTHING,
        ),
    )

    fun withFormantGainLimit(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        formantGainLimit = value.coerceIn(
            MIN_FORMANT_GAIN_LIMIT,
            MAX_FORMANT_GAIN_LIMIT,
        ),
    )

    fun withFormantFftSize(value: Int): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        formantFftSize = normalizeFftSize(value),
    )

    fun withFormantOverlap(value: Int): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        formantOverlap = normalizeFormantOverlap(value),
    )

    fun withPitchFftSize(value: Int): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        pitchFftSize = normalizeFftSize(value),
    )

    fun withPitchOverlap(value: Int): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        pitchOverlap = normalizePitchOverlap(value),
    )

    companion object {
        const val MIN_TONE = -12.0
        const val MAX_TONE = 12.0
        const val MIN_SPEED = 0.5
        const val MAX_SPEED = 1.5
        const val MIN_EFFECT = 0.0
        const val MAX_EFFECT = 1.0
        const val MIN_HIGH_PASS_HZ = 0.0
        const val MAX_HIGH_PASS_HZ = 300.0
        const val MIN_NOISE_GATE_DB = -80.0
        const val MAX_NOISE_GATE_DB = -20.0
        const val MIN_COMPRESSOR = 0.0
        const val MAX_COMPRESSOR = 1.0
        const val MIN_OUTPUT_GAIN_DB = -12.0
        const val MAX_OUTPUT_GAIN_DB = 12.0
        const val MIN_FORMANT_MIX = 0.0
        const val MAX_FORMANT_MIX = 1.0
        const val MIN_SPECTRAL_SMOOTHING = 0.0
        const val MAX_SPECTRAL_SMOOTHING = 1.0
        const val MIN_FORMANT_GAIN_LIMIT = 1.5
        const val MAX_FORMANT_GAIN_LIMIT = 4.0

        const val DEFAULT_TONE = -4.0
        const val DEFAULT_FORMANT = 2.0
        const val DEFAULT_SPEED = 1.0
        const val DEFAULT_GRIT = 0.0
        const val DEFAULT_ROBOT = 0.0
        const val DEFAULT_ECHO = 0.0
        const val DEFAULT_FAN = 0.0
        const val DEFAULT_STUTTER = 0.0
        const val DEFAULT_REVERSE = false
        const val DEFAULT_ALIEN = 0.0
        const val DEFAULT_DRUNK = 0.0
        const val DEFAULT_TELEPHONE = 0.0
        const val DEFAULT_MEGAPHONE = 0.0
        const val DEFAULT_BEE = 0.0
        const val DEFAULT_HIGH_PASS_HZ = 0.0
        const val DEFAULT_NOISE_GATE_DB = -80.0
        const val DEFAULT_COMPRESSOR = 0.0
        const val DEFAULT_OUTPUT_GAIN_DB = 0.0
        const val DEFAULT_FORMANT_MIX = 1.0
        const val DEFAULT_SPECTRAL_SMOOTHING = 0.5
        const val DEFAULT_FORMANT_GAIN_LIMIT = 4.0
        const val DEFAULT_FORMANT_FFT_SIZE = 2048
        const val DEFAULT_FORMANT_OVERLAP = 4
        const val DEFAULT_PITCH_FFT_SIZE = 2048
        const val DEFAULT_PITCH_OVERLAP = 4

        val FFT_SIZES = listOf(1024, 2048, 4096)
        val FORMANT_OVERLAPS = listOf(2, 4, 8)
        val PITCH_OVERLAPS = listOf(4, 8)

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

        internal fun normalizeFftSize(value: Int): Int = when {
            value <= 1024 -> 1024
            value <= 2048 -> 2048
            else -> 4096
        }

        internal fun normalizeFormantOverlap(value: Int): Int = when {
            value <= 2 -> 2
            value <= 4 -> 4
            else -> 8
        }

        internal fun normalizePitchOverlap(value: Int): Int = if (value <= 4) 4 else 8

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
                formantSemitones = -2.0,
                speed = 1.0,
                grit = DEFAULT_GRIT,
                robot = DEFAULT_ROBOT,
            )
            VoicePreset.CHIPMUNK -> VoiceSettings(
                preset = VoicePreset.CHIPMUNK,
                toneSemitones = 12.0,
                formantSemitones = 6.0,
                speed = 1.1,
            )
            VoicePreset.MONSTER -> VoiceSettings(
                preset = VoicePreset.MONSTER,
                toneSemitones = -12.0,
                formantSemitones = -6.0,
                speed = 0.85,
                grit = 0.35,
            )
            VoicePreset.KID -> VoiceSettings(
                preset = VoicePreset.KID,
                toneSemitones = 6.0,
                formantSemitones = 4.0,
                speed = 1.05,
            )
            VoicePreset.OLD_MAN -> VoiceSettings(
                preset = VoicePreset.OLD_MAN,
                toneSemitones = -6.0,
                formantSemitones = -3.0,
                speed = 0.9,
                drunk = 0.25,
            )
            VoicePreset.STUTTER -> VoiceSettings(
                preset = VoicePreset.STUTTER,
                toneSemitones = DEFAULT_TONE,
                formantSemitones = DEFAULT_FORMANT,
                speed = DEFAULT_SPEED,
                stutter = 0.8,
            )
            VoicePreset.REVERSE -> VoiceSettings(
                preset = VoicePreset.REVERSE,
                toneSemitones = DEFAULT_TONE,
                formantSemitones = DEFAULT_FORMANT,
                speed = DEFAULT_SPEED,
                reverse = true,
            )
            VoicePreset.ALIEN -> VoiceSettings(
                preset = VoicePreset.ALIEN,
                toneSemitones = -2.0,
                formantSemitones = 2.0,
                speed = 1.0,
                alien = 0.7,
            )
            VoicePreset.DRUNK -> VoiceSettings(
                preset = VoicePreset.DRUNK,
                toneSemitones = DEFAULT_TONE,
                formantSemitones = DEFAULT_FORMANT,
                speed = DEFAULT_SPEED,
                drunk = 0.7,
            )
            VoicePreset.GHOST -> VoiceSettings(
                preset = VoicePreset.GHOST,
                toneSemitones = -5.0,
                formantSemitones = -2.0,
                speed = 0.95,
                echo = 0.55,
                fan = 0.12,
            )
            VoicePreset.TELEPHONE -> VoiceSettings(
                preset = VoicePreset.TELEPHONE,
                toneSemitones = DEFAULT_TONE,
                formantSemitones = DEFAULT_FORMANT,
                speed = DEFAULT_SPEED,
                telephone = 1.0,
            )
            VoicePreset.MEGAPHONE -> VoiceSettings(
                preset = VoicePreset.MEGAPHONE,
                toneSemitones = DEFAULT_TONE,
                formantSemitones = DEFAULT_FORMANT,
                speed = DEFAULT_SPEED,
                megaphone = 1.0,
            )
            VoicePreset.BEE -> VoiceSettings(
                preset = VoicePreset.BEE,
                toneSemitones = DEFAULT_TONE,
                formantSemitones = DEFAULT_FORMANT,
                speed = DEFAULT_SPEED,
                bee = 0.7,
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
