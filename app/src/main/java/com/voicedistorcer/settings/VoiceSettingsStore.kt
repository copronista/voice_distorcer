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
        val gritDrive = preferences.getFloat(
            KEY_GRIT_DRIVE,
            fallback.gritDrive.toFloat(),
        ).toDouble()
        val echoDelay = preferences.getFloat(
            KEY_ECHO_DELAY,
            fallback.echoDelayMs.toFloat(),
        ).toDouble()
        val echoWet = preferences.getFloat(KEY_ECHO_WET, fallback.echoWet.toFloat()).toDouble()
        val echoFeedback = preferences.getFloat(
            KEY_ECHO_FEEDBACK,
            fallback.echoFeedback.toFloat(),
        ).toDouble()
        val fanCutoff = preferences.getFloat(
            KEY_FAN_CUTOFF,
            fallback.fanCutoffHz.toFloat(),
        ).toDouble()
        val fanModulation = preferences.getFloat(
            KEY_FAN_MODULATION,
            fallback.fanModulationHz.toFloat(),
        ).toDouble()
        val fanNoiseLevel = preferences.getFloat(
            KEY_FAN_NOISE_LEVEL,
            fallback.fanNoiseLevel.toFloat(),
        ).toDouble()
        val fanNoiseSeed = preferences.getInt(KEY_FAN_NOISE_SEED, fallback.fanNoiseSeed)
        val stutterSlice = preferences.getFloat(
            KEY_STUTTER_SLICE,
            fallback.stutterSliceMs.toFloat(),
        ).toDouble()
        val stutterSeed = preferences.getInt(KEY_STUTTER_SEED, fallback.stutterSeed)
        val alienCarrier = preferences.getFloat(
            KEY_ALIEN_CARRIER,
            fallback.alienCarrierHz.toFloat(),
        ).toDouble()
        val alienWobble = preferences.getFloat(
            KEY_ALIEN_WOBBLE,
            fallback.alienWobbleHz.toFloat(),
        ).toDouble()
        val alienTremolo = preferences.getFloat(
            KEY_ALIEN_TREMOLO,
            fallback.alienTremoloHz.toFloat(),
        ).toDouble()
        val drunkMaxDelay = preferences.getFloat(
            KEY_DRUNK_MAX_DELAY,
            fallback.drunkMaxDelayMs.toFloat(),
        ).toDouble()
        val drunkWobble = preferences.getFloat(
            KEY_DRUNK_WOBBLE,
            fallback.drunkWobbleHz.toFloat(),
        ).toDouble()
        val drunkTremolo = preferences.getFloat(
            KEY_DRUNK_TREMOLO,
            fallback.drunkTremoloHz.toFloat(),
        ).toDouble()
        val telephoneLow = preferences.getFloat(
            KEY_TELEPHONE_LOW,
            fallback.telephoneLowHz.toFloat(),
        ).toDouble()
        val telephoneHigh = preferences.getFloat(
            KEY_TELEPHONE_HIGH,
            fallback.telephoneHighHz.toFloat(),
        ).toDouble()
        val telephoneDrive = preferences.getFloat(
            KEY_TELEPHONE_DRIVE,
            fallback.telephoneDrive.toFloat(),
        ).toDouble()
        val megaphoneLow = preferences.getFloat(
            KEY_MEGAPHONE_LOW,
            fallback.megaphoneLowHz.toFloat(),
        ).toDouble()
        val megaphoneHigh = preferences.getFloat(
            KEY_MEGAPHONE_HIGH,
            fallback.megaphoneHighHz.toFloat(),
        ).toDouble()
        val megaphoneDrive = preferences.getFloat(
            KEY_MEGAPHONE_DRIVE,
            fallback.megaphoneDrive.toFloat(),
        ).toDouble()
        val beeBuzz = preferences.getFloat(
            KEY_BEE_BUZZ,
            fallback.beeBuzzHz.toFloat(),
        ).toDouble()
        val beeBuzzRatio = preferences.getFloat(
            KEY_BEE_BUZZ_RATIO,
            fallback.beeBuzzRatio.toFloat(),
        ).toDouble()
        val beeFlutter = preferences.getFloat(
            KEY_BEE_FLUTTER,
            fallback.beeFlutterHz.toFloat(),
        ).toDouble()
        val compressorMaxRatio = preferences.getFloat(
            KEY_COMPRESSOR_MAX_RATIO,
            fallback.compressorMaxRatio.toFloat(),
        ).toDouble()
        val compressorThreshold = preferences.getFloat(
            KEY_COMPRESSOR_THRESHOLD,
            fallback.compressorThreshold.toFloat(),
        ).toDouble()
        val compressorAttack = preferences.getFloat(
            KEY_COMPRESSOR_ATTACK,
            fallback.compressorAttackMs.toFloat(),
        ).toDouble()
        val compressorRelease = preferences.getFloat(
            KEY_COMPRESSOR_RELEASE,
            fallback.compressorReleaseMs.toFloat(),
        ).toDouble()
        val gateAttack = preferences.getFloat(
            KEY_GATE_ATTACK,
            fallback.gateAttackMs.toFloat(),
        ).toDouble()
        val gateRelease = preferences.getFloat(
            KEY_GATE_RELEASE,
            fallback.gateReleaseMs.toFloat(),
        ).toDouble()
        val robotCarrier = preferences.getFloat(
            KEY_ROBOT_CARRIER,
            fallback.robotCarrierHz.toFloat(),
        ).toDouble()
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
            gritDrive = gritDrive.coerceIn(
                VoiceSettings.MIN_GRIT_DRIVE,
                VoiceSettings.MAX_GRIT_DRIVE,
            ),
            echoDelayMs = echoDelay.coerceIn(
                VoiceSettings.MIN_ECHO_DELAY_MS,
                VoiceSettings.MAX_ECHO_DELAY_MS,
            ),
            echoWet = echoWet.coerceIn(VoiceSettings.MIN_ECHO_WET, VoiceSettings.MAX_ECHO_WET),
            echoFeedback = echoFeedback.coerceIn(
                VoiceSettings.MIN_ECHO_FEEDBACK,
                VoiceSettings.MAX_ECHO_FEEDBACK,
            ),
            fanCutoffHz = fanCutoff.coerceIn(
                VoiceSettings.MIN_FAN_CUTOFF_HZ,
                VoiceSettings.MAX_FAN_CUTOFF_HZ,
            ),
            fanModulationHz = fanModulation.coerceIn(
                VoiceSettings.MIN_FAN_MODULATION_HZ,
                VoiceSettings.MAX_FAN_MODULATION_HZ,
            ),
            fanNoiseLevel = fanNoiseLevel.coerceIn(
                VoiceSettings.MIN_FAN_NOISE_LEVEL,
                VoiceSettings.MAX_FAN_NOISE_LEVEL,
            ),
            fanNoiseSeed = fanNoiseSeed,
            stutterSliceMs = stutterSlice.coerceIn(
                VoiceSettings.MIN_STUTTER_SLICE_MS,
                VoiceSettings.MAX_STUTTER_SLICE_MS,
            ),
            stutterSeed = stutterSeed,
            alienCarrierHz = alienCarrier.coerceIn(
                VoiceSettings.MIN_ALIEN_CARRIER_HZ,
                VoiceSettings.MAX_ALIEN_CARRIER_HZ,
            ),
            alienWobbleHz = alienWobble.coerceIn(
                VoiceSettings.MIN_ALIEN_WOBBLE_HZ,
                VoiceSettings.MAX_ALIEN_WOBBLE_HZ,
            ),
            alienTremoloHz = alienTremolo.coerceIn(
                VoiceSettings.MIN_ALIEN_TREMOLO_HZ,
                VoiceSettings.MAX_ALIEN_TREMOLO_HZ,
            ),
            drunkMaxDelayMs = drunkMaxDelay.coerceIn(
                VoiceSettings.MIN_DRUNK_MAX_DELAY_MS,
                VoiceSettings.MAX_DRUNK_MAX_DELAY_MS,
            ),
            drunkWobbleHz = drunkWobble.coerceIn(
                VoiceSettings.MIN_DRUNK_WOBBLE_HZ,
                VoiceSettings.MAX_DRUNK_WOBBLE_HZ,
            ),
            drunkTremoloHz = drunkTremolo.coerceIn(
                VoiceSettings.MIN_DRUNK_TREMOLO_HZ,
                VoiceSettings.MAX_DRUNK_TREMOLO_HZ,
            ),
            telephoneLowHz = telephoneLow.coerceIn(
                VoiceSettings.MIN_TELEPHONE_LOW_HZ,
                VoiceSettings.MAX_TELEPHONE_LOW_HZ,
            ),
            telephoneHighHz = telephoneHigh.coerceIn(
                VoiceSettings.MIN_TELEPHONE_HIGH_HZ,
                VoiceSettings.MAX_TELEPHONE_HIGH_HZ,
            ),
            telephoneDrive = telephoneDrive.coerceIn(
                VoiceSettings.MIN_TELEPHONE_DRIVE,
                VoiceSettings.MAX_TELEPHONE_DRIVE,
            ),
            megaphoneLowHz = megaphoneLow.coerceIn(
                VoiceSettings.MIN_MEGAPHONE_LOW_HZ,
                VoiceSettings.MAX_MEGAPHONE_LOW_HZ,
            ),
            megaphoneHighHz = megaphoneHigh.coerceIn(
                VoiceSettings.MIN_MEGAPHONE_HIGH_HZ,
                VoiceSettings.MAX_MEGAPHONE_HIGH_HZ,
            ),
            megaphoneDrive = megaphoneDrive.coerceIn(
                VoiceSettings.MIN_MEGAPHONE_DRIVE,
                VoiceSettings.MAX_MEGAPHONE_DRIVE,
            ),
            beeBuzzHz = beeBuzz.coerceIn(
                VoiceSettings.MIN_BEE_BUZZ_HZ,
                VoiceSettings.MAX_BEE_BUZZ_HZ,
            ),
            beeBuzzRatio = beeBuzzRatio.coerceIn(
                VoiceSettings.MIN_BEE_BUZZ_RATIO,
                VoiceSettings.MAX_BEE_BUZZ_RATIO,
            ),
            beeFlutterHz = beeFlutter.coerceIn(
                VoiceSettings.MIN_BEE_FLUTTER_HZ,
                VoiceSettings.MAX_BEE_FLUTTER_HZ,
            ),
            compressorMaxRatio = compressorMaxRatio.coerceIn(
                VoiceSettings.MIN_COMPRESSOR_MAX_RATIO,
                VoiceSettings.MAX_COMPRESSOR_MAX_RATIO,
            ),
            compressorThreshold = compressorThreshold.coerceIn(
                VoiceSettings.MIN_COMPRESSOR_THRESHOLD,
                VoiceSettings.MAX_COMPRESSOR_THRESHOLD,
            ),
            compressorAttackMs = compressorAttack.coerceIn(
                VoiceSettings.MIN_COMPRESSOR_ATTACK_MS,
                VoiceSettings.MAX_COMPRESSOR_ATTACK_MS,
            ),
            compressorReleaseMs = compressorRelease.coerceIn(
                VoiceSettings.MIN_COMPRESSOR_RELEASE_MS,
                VoiceSettings.MAX_COMPRESSOR_RELEASE_MS,
            ),
            gateAttackMs = gateAttack.coerceIn(
                VoiceSettings.MIN_GATE_ATTACK_MS,
                VoiceSettings.MAX_GATE_ATTACK_MS,
            ),
            gateReleaseMs = gateRelease.coerceIn(
                VoiceSettings.MIN_GATE_RELEASE_MS,
                VoiceSettings.MAX_GATE_RELEASE_MS,
            ),
            robotCarrierHz = robotCarrier.coerceIn(
                VoiceSettings.MIN_ROBOT_CARRIER_HZ,
                VoiceSettings.MAX_ROBOT_CARRIER_HZ,
            ),
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
            .putFloat(KEY_GRIT_DRIVE, settings.gritDrive.toFloat())
            .putFloat(KEY_ECHO_DELAY, settings.echoDelayMs.toFloat())
            .putFloat(KEY_ECHO_WET, settings.echoWet.toFloat())
            .putFloat(KEY_ECHO_FEEDBACK, settings.echoFeedback.toFloat())
            .putFloat(KEY_FAN_CUTOFF, settings.fanCutoffHz.toFloat())
            .putFloat(KEY_FAN_MODULATION, settings.fanModulationHz.toFloat())
            .putFloat(KEY_FAN_NOISE_LEVEL, settings.fanNoiseLevel.toFloat())
            .putInt(KEY_FAN_NOISE_SEED, settings.fanNoiseSeed)
            .putFloat(KEY_STUTTER_SLICE, settings.stutterSliceMs.toFloat())
            .putInt(KEY_STUTTER_SEED, settings.stutterSeed)
            .putFloat(KEY_ALIEN_CARRIER, settings.alienCarrierHz.toFloat())
            .putFloat(KEY_ALIEN_WOBBLE, settings.alienWobbleHz.toFloat())
            .putFloat(KEY_ALIEN_TREMOLO, settings.alienTremoloHz.toFloat())
            .putFloat(KEY_DRUNK_MAX_DELAY, settings.drunkMaxDelayMs.toFloat())
            .putFloat(KEY_DRUNK_WOBBLE, settings.drunkWobbleHz.toFloat())
            .putFloat(KEY_DRUNK_TREMOLO, settings.drunkTremoloHz.toFloat())
            .putFloat(KEY_TELEPHONE_LOW, settings.telephoneLowHz.toFloat())
            .putFloat(KEY_TELEPHONE_HIGH, settings.telephoneHighHz.toFloat())
            .putFloat(KEY_TELEPHONE_DRIVE, settings.telephoneDrive.toFloat())
            .putFloat(KEY_MEGAPHONE_LOW, settings.megaphoneLowHz.toFloat())
            .putFloat(KEY_MEGAPHONE_HIGH, settings.megaphoneHighHz.toFloat())
            .putFloat(KEY_MEGAPHONE_DRIVE, settings.megaphoneDrive.toFloat())
            .putFloat(KEY_BEE_BUZZ, settings.beeBuzzHz.toFloat())
            .putFloat(KEY_BEE_BUZZ_RATIO, settings.beeBuzzRatio.toFloat())
            .putFloat(KEY_BEE_FLUTTER, settings.beeFlutterHz.toFloat())
            .putFloat(KEY_COMPRESSOR_MAX_RATIO, settings.compressorMaxRatio.toFloat())
            .putFloat(KEY_COMPRESSOR_THRESHOLD, settings.compressorThreshold.toFloat())
            .putFloat(KEY_COMPRESSOR_ATTACK, settings.compressorAttackMs.toFloat())
            .putFloat(KEY_COMPRESSOR_RELEASE, settings.compressorReleaseMs.toFloat())
            .putFloat(KEY_GATE_ATTACK, settings.gateAttackMs.toFloat())
            .putFloat(KEY_GATE_RELEASE, settings.gateReleaseMs.toFloat())
            .putFloat(KEY_ROBOT_CARRIER, settings.robotCarrierHz.toFloat())
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
        const val KEY_GRIT_DRIVE = "grit_drive"
        const val KEY_ECHO_DELAY = "echo_delay"
        const val KEY_ECHO_WET = "echo_wet"
        const val KEY_ECHO_FEEDBACK = "echo_feedback"
        const val KEY_FAN_CUTOFF = "fan_cutoff"
        const val KEY_FAN_MODULATION = "fan_modulation"
        const val KEY_FAN_NOISE_LEVEL = "fan_noise_level"
        const val KEY_FAN_NOISE_SEED = "fan_noise_seed"
        const val KEY_STUTTER_SLICE = "stutter_slice"
        const val KEY_STUTTER_SEED = "stutter_seed"
        const val KEY_ALIEN_CARRIER = "alien_carrier"
        const val KEY_ALIEN_WOBBLE = "alien_wobble"
        const val KEY_ALIEN_TREMOLO = "alien_tremolo"
        const val KEY_DRUNK_MAX_DELAY = "drunk_max_delay"
        const val KEY_DRUNK_WOBBLE = "drunk_wobble"
        const val KEY_DRUNK_TREMOLO = "drunk_tremolo"
        const val KEY_TELEPHONE_LOW = "telephone_low"
        const val KEY_TELEPHONE_HIGH = "telephone_high"
        const val KEY_TELEPHONE_DRIVE = "telephone_drive"
        const val KEY_MEGAPHONE_LOW = "megaphone_low"
        const val KEY_MEGAPHONE_HIGH = "megaphone_high"
        const val KEY_MEGAPHONE_DRIVE = "megaphone_drive"
        const val KEY_BEE_BUZZ = "bee_buzz"
        const val KEY_BEE_BUZZ_RATIO = "bee_buzz_ratio"
        const val KEY_BEE_FLUTTER = "bee_flutter"
        const val KEY_COMPRESSOR_MAX_RATIO = "compressor_max_ratio"
        const val KEY_COMPRESSOR_THRESHOLD = "compressor_threshold"
        const val KEY_COMPRESSOR_ATTACK = "compressor_attack"
        const val KEY_COMPRESSOR_RELEASE = "compressor_release"
        const val KEY_GATE_ATTACK = "gate_attack"
        const val KEY_GATE_RELEASE = "gate_release"
        const val KEY_ROBOT_CARRIER = "robot_carrier"
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
