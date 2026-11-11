package com.voicedistorcer.settings

import android.content.Context

class VoiceSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): VoiceSettings {
        val preset = runCatching {
            VoicePreset.valueOf(preferences.getString(KEY_PRESET, VoicePreset.DEFAULT.name)!!)
        }.getOrDefault(VoicePreset.DEFAULT)
        val fallback = VoiceSettings.forPreset(
            if (preset == VoicePreset.CUSTOM) VoicePreset.DEFAULT else preset,
        )
        val tone = preferences.getFloat(KEY_TONE, fallback.toneSemitones.toFloat()).toDouble()
        val formant = preferences.getFloat(KEY_FORMANT, fallback.formantSemitones.toFloat()).toDouble()
        val speed = preferences.getFloat(KEY_SPEED, fallback.speed.toFloat()).toDouble()
        val grit = preferences.getFloat(KEY_GRIT, fallback.grit.toFloat()).toDouble()
        val robot = preferences.getFloat(KEY_ROBOT, fallback.robot.toFloat()).toDouble()
        val echo = preferences.getFloat(KEY_ECHO, fallback.echo.toFloat()).toDouble()
        val fan = preferences.getFloat(KEY_FAN, fallback.fan.toFloat()).toDouble()
        val stutter = preferences.getFloat(KEY_STUTTER, fallback.stutter.toFloat()).toDouble()
        val reverse = preferences.getBoolean(KEY_REVERSE, fallback.reverse)
        val alien = preferences.getFloat(KEY_ALIEN, fallback.alien.toFloat()).toDouble()
        val drunk = preferences.getFloat(KEY_DRUNK, fallback.drunk.toFloat()).toDouble()
        val telephone = preferences.getFloat(
            KEY_TELEPHONE,
            fallback.telephone.toFloat(),
        ).toDouble()
        val megaphone = preferences.getFloat(
            KEY_MEGAPHONE,
            fallback.megaphone.toFloat(),
        ).toDouble()
        val bee = preferences.getFloat(KEY_BEE, fallback.bee.toFloat()).toDouble()
        val highPass = preferences.getFloat(
            KEY_HIGH_PASS,
            fallback.highPassHz.toFloat(),
        ).toDouble()
        val noiseGate = preferences.getFloat(
            KEY_NOISE_GATE,
            fallback.noiseGateDb.toFloat(),
        ).toDouble()
        val compressor = preferences.getFloat(
            KEY_COMPRESSOR,
            fallback.compressor.toFloat(),
        ).toDouble()
        val outputGain = preferences.getFloat(
            KEY_OUTPUT_GAIN,
            fallback.outputGainDb.toFloat(),
        ).toDouble()
        val formantMix = preferences.getFloat(
            KEY_FORMANT_MIX,
            fallback.formantMix.toFloat(),
        ).toDouble()
        val smoothing = preferences.getFloat(
            KEY_SPECTRAL_SMOOTHING,
            fallback.spectralSmoothing.toFloat(),
        ).toDouble()
        val gainLimit = preferences.getFloat(
            KEY_FORMANT_GAIN_LIMIT,
            fallback.formantGainLimit.toFloat(),
        ).toDouble()

        val safeTone = tone.coerceIn(VoiceSettings.MIN_TONE, VoiceSettings.MAX_TONE)
        return VoiceSettings(
            preset = preset,
            toneSemitones = safeTone,
            formantSemitones = VoiceSettings.sanitizeFormant(safeTone, formant),
            speed = speed.coerceIn(VoiceSettings.MIN_SPEED, VoiceSettings.MAX_SPEED),
            grit = grit.coerceIn(VoiceSettings.MIN_EFFECT, VoiceSettings.MAX_EFFECT),
            robot = robot.coerceIn(VoiceSettings.MIN_EFFECT, VoiceSettings.MAX_EFFECT),
            echo = echo.coerceIn(VoiceSettings.MIN_EFFECT, VoiceSettings.MAX_EFFECT),
            fan = fan.coerceIn(VoiceSettings.MIN_EFFECT, VoiceSettings.MAX_EFFECT),
            stutter = stutter.coerceIn(VoiceSettings.MIN_EFFECT, VoiceSettings.MAX_EFFECT),
            reverse = reverse,
            alien = alien.coerceIn(VoiceSettings.MIN_EFFECT, VoiceSettings.MAX_EFFECT),
            drunk = drunk.coerceIn(VoiceSettings.MIN_EFFECT, VoiceSettings.MAX_EFFECT),
            telephone = telephone.coerceIn(VoiceSettings.MIN_EFFECT, VoiceSettings.MAX_EFFECT),
            megaphone = megaphone.coerceIn(VoiceSettings.MIN_EFFECT, VoiceSettings.MAX_EFFECT),
            bee = bee.coerceIn(VoiceSettings.MIN_EFFECT, VoiceSettings.MAX_EFFECT),
            highPassHz = highPass.coerceIn(
                VoiceSettings.MIN_HIGH_PASS_HZ,
                VoiceSettings.MAX_HIGH_PASS_HZ,
            ),
            noiseGateDb = noiseGate.coerceIn(
                VoiceSettings.MIN_NOISE_GATE_DB,
                VoiceSettings.MAX_NOISE_GATE_DB,
            ),
            compressor = compressor.coerceIn(
                VoiceSettings.MIN_COMPRESSOR,
                VoiceSettings.MAX_COMPRESSOR,
            ),
            outputGainDb = outputGain.coerceIn(
                VoiceSettings.MIN_OUTPUT_GAIN_DB,
                VoiceSettings.MAX_OUTPUT_GAIN_DB,
            ),
            formantMix = formantMix.coerceIn(
                VoiceSettings.MIN_FORMANT_MIX,
                VoiceSettings.MAX_FORMANT_MIX,
            ),
            spectralSmoothing = smoothing.coerceIn(
                VoiceSettings.MIN_SPECTRAL_SMOOTHING,
                VoiceSettings.MAX_SPECTRAL_SMOOTHING,
            ),
            formantGainLimit = gainLimit.coerceIn(
                VoiceSettings.MIN_FORMANT_GAIN_LIMIT,
                VoiceSettings.MAX_FORMANT_GAIN_LIMIT,
            ),
            formantFftSize = VoiceSettings.normalizeFftSize(
                preferences.getInt(KEY_FORMANT_FFT, fallback.formantFftSize),
            ),
            formantOverlap = VoiceSettings.normalizeFormantOverlap(
                preferences.getInt(KEY_FORMANT_OVERLAP, fallback.formantOverlap),
            ),
            pitchFftSize = VoiceSettings.normalizeFftSize(
                preferences.getInt(KEY_PITCH_FFT, fallback.pitchFftSize),
            ),
            pitchOverlap = VoiceSettings.normalizePitchOverlap(
                preferences.getInt(KEY_PITCH_OVERLAP, fallback.pitchOverlap),
            ),
        )
    }

    fun save(settings: VoiceSettings) {
        preferences.edit()
            .putString(KEY_PRESET, settings.preset.name)
            .putFloat(KEY_TONE, settings.toneSemitones.toFloat())
            .putFloat(KEY_FORMANT, settings.formantSemitones.toFloat())
            .putFloat(KEY_SPEED, settings.speed.toFloat())
            .putFloat(KEY_GRIT, settings.grit.toFloat())
            .putFloat(KEY_ROBOT, settings.robot.toFloat())
            .putFloat(KEY_ECHO, settings.echo.toFloat())
            .putFloat(KEY_FAN, settings.fan.toFloat())
            .putFloat(KEY_STUTTER, settings.stutter.toFloat())
            .putBoolean(KEY_REVERSE, settings.reverse)
            .putFloat(KEY_ALIEN, settings.alien.toFloat())
            .putFloat(KEY_DRUNK, settings.drunk.toFloat())
            .putFloat(KEY_TELEPHONE, settings.telephone.toFloat())
            .putFloat(KEY_MEGAPHONE, settings.megaphone.toFloat())
            .putFloat(KEY_BEE, settings.bee.toFloat())
            .putFloat(KEY_HIGH_PASS, settings.highPassHz.toFloat())
            .putFloat(KEY_NOISE_GATE, settings.noiseGateDb.toFloat())
            .putFloat(KEY_COMPRESSOR, settings.compressor.toFloat())
            .putFloat(KEY_OUTPUT_GAIN, settings.outputGainDb.toFloat())
            .putFloat(KEY_FORMANT_MIX, settings.formantMix.toFloat())
            .putFloat(KEY_SPECTRAL_SMOOTHING, settings.spectralSmoothing.toFloat())
            .putFloat(KEY_FORMANT_GAIN_LIMIT, settings.formantGainLimit.toFloat())
            .putInt(KEY_FORMANT_FFT, settings.formantFftSize)
            .putInt(KEY_FORMANT_OVERLAP, settings.formantOverlap)
            .putInt(KEY_PITCH_FFT, settings.pitchFftSize)
            .putInt(KEY_PITCH_OVERLAP, settings.pitchOverlap)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "voice_settings"
        const val KEY_PRESET = "preset"
        const val KEY_TONE = "tone"
        const val KEY_FORMANT = "formant"
        const val KEY_SPEED = "speed"
        const val KEY_GRIT = "grit"
        const val KEY_ROBOT = "robot"
        const val KEY_ECHO = "echo"
        const val KEY_FAN = "fan"
        const val KEY_STUTTER = "stutter"
        const val KEY_REVERSE = "reverse"
        const val KEY_ALIEN = "alien"
        const val KEY_DRUNK = "drunk"
        const val KEY_TELEPHONE = "telephone"
        const val KEY_MEGAPHONE = "megaphone"
        const val KEY_BEE = "bee"
        const val KEY_HIGH_PASS = "high_pass"
        const val KEY_NOISE_GATE = "noise_gate"
        const val KEY_COMPRESSOR = "compressor"
        const val KEY_OUTPUT_GAIN = "output_gain"
        const val KEY_FORMANT_MIX = "formant_mix"
        const val KEY_SPECTRAL_SMOOTHING = "spectral_smoothing"
        const val KEY_FORMANT_GAIN_LIMIT = "formant_gain_limit"
        const val KEY_FORMANT_FFT = "formant_fft"
        const val KEY_FORMANT_OVERLAP = "formant_overlap"
        const val KEY_PITCH_FFT = "pitch_fft"
        const val KEY_PITCH_OVERLAP = "pitch_overlap"
    }
}
