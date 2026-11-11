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

        val safeTone = tone.coerceIn(VoiceSettings.MIN_TONE, VoiceSettings.MAX_TONE)
        return VoiceSettings(
            preset = preset,
            toneSemitones = safeTone,
            formantSemitones = VoiceSettings.sanitizeFormant(safeTone, formant),
            speed = speed.coerceIn(VoiceSettings.MIN_SPEED, VoiceSettings.MAX_SPEED),
            grit = grit.coerceIn(VoiceSettings.MIN_EFFECT, VoiceSettings.MAX_EFFECT),
            robot = robot.coerceIn(VoiceSettings.MIN_EFFECT, VoiceSettings.MAX_EFFECT),
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
    }
}
