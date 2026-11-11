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
    val gritDrive: Double = DEFAULT_GRIT_DRIVE,
    val echoDelayMs: Double = DEFAULT_ECHO_DELAY_MS,
    val echoWet: Double = DEFAULT_ECHO_WET,
    val echoFeedback: Double = DEFAULT_ECHO_FEEDBACK,
    val fanCutoffHz: Double = DEFAULT_FAN_CUTOFF_HZ,
    val fanModulationHz: Double = DEFAULT_FAN_MODULATION_HZ,
    val fanNoiseLevel: Double = DEFAULT_FAN_NOISE_LEVEL,
    val fanNoiseSeed: Int = DEFAULT_FAN_NOISE_SEED,
    val stutterSliceMs: Double = DEFAULT_STUTTER_SLICE_MS,
    val stutterSeed: Int = DEFAULT_STUTTER_SEED,
    val alienCarrierHz: Double = DEFAULT_ALIEN_CARRIER_HZ,
    val alienWobbleHz: Double = DEFAULT_ALIEN_WOBBLE_HZ,
    val alienTremoloHz: Double = DEFAULT_ALIEN_TREMOLO_HZ,
    val drunkMaxDelayMs: Double = DEFAULT_DRUNK_MAX_DELAY_MS,
    val drunkWobbleHz: Double = DEFAULT_DRUNK_WOBBLE_HZ,
    val drunkTremoloHz: Double = DEFAULT_DRUNK_TREMOLO_HZ,
    val telephoneLowHz: Double = DEFAULT_TELEPHONE_LOW_HZ,
    val telephoneHighHz: Double = DEFAULT_TELEPHONE_HIGH_HZ,
    val telephoneDrive: Double = DEFAULT_TELEPHONE_DRIVE,
    val megaphoneLowHz: Double = DEFAULT_MEGAPHONE_LOW_HZ,
    val megaphoneHighHz: Double = DEFAULT_MEGAPHONE_HIGH_HZ,
    val megaphoneDrive: Double = DEFAULT_MEGAPHONE_DRIVE,
    val beeBuzzHz: Double = DEFAULT_BEE_BUZZ_HZ,
    val beeBuzzRatio: Double = DEFAULT_BEE_BUZZ_RATIO,
    val beeFlutterHz: Double = DEFAULT_BEE_FLUTTER_HZ,
    val compressorMaxRatio: Double = DEFAULT_COMPRESSOR_MAX_RATIO,
    val compressorThreshold: Double = DEFAULT_COMPRESSOR_THRESHOLD,
    val compressorAttackMs: Double = DEFAULT_COMPRESSOR_ATTACK_MS,
    val compressorReleaseMs: Double = DEFAULT_COMPRESSOR_RELEASE_MS,
    val gateAttackMs: Double = DEFAULT_GATE_ATTACK_MS,
    val gateReleaseMs: Double = DEFAULT_GATE_RELEASE_MS,
    val robotCarrierHz: Double = DEFAULT_ROBOT_CARRIER_HZ,
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
        val telephoneLow = telephoneLowHz.coerceIn(MIN_TELEPHONE_LOW_HZ, MAX_TELEPHONE_LOW_HZ)
        val telephoneHigh = telephoneHighHz
            .coerceIn(MIN_TELEPHONE_HIGH_HZ, MAX_TELEPHONE_HIGH_HZ)
            .coerceAtLeast(telephoneLow + 1.0)
        val megaphoneLow = megaphoneLowHz.coerceIn(MIN_MEGAPHONE_LOW_HZ, MAX_MEGAPHONE_LOW_HZ)
        val megaphoneHigh = megaphoneHighHz
            .coerceIn(MIN_MEGAPHONE_HIGH_HZ, MAX_MEGAPHONE_HIGH_HZ)
            .coerceAtLeast(megaphoneLow + 1.0)
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
            gritDrive = gritDrive.coerceIn(MIN_GRIT_DRIVE, MAX_GRIT_DRIVE),
            echoDelayMs = echoDelayMs.coerceIn(MIN_ECHO_DELAY_MS, MAX_ECHO_DELAY_MS),
            echoWet = echoWet.coerceIn(MIN_ECHO_WET, MAX_ECHO_WET),
            echoFeedback = echoFeedback.coerceIn(MIN_ECHO_FEEDBACK, MAX_ECHO_FEEDBACK),
            fanCutoffHz = fanCutoffHz.coerceIn(MIN_FAN_CUTOFF_HZ, MAX_FAN_CUTOFF_HZ),
            fanModulationHz = fanModulationHz.coerceIn(
                MIN_FAN_MODULATION_HZ,
                MAX_FAN_MODULATION_HZ,
            ),
            fanNoiseLevel = fanNoiseLevel.coerceIn(MIN_FAN_NOISE_LEVEL, MAX_FAN_NOISE_LEVEL),
            fanNoiseSeed = fanNoiseSeed,
            stutterSliceMs = stutterSliceMs.coerceIn(
                MIN_STUTTER_SLICE_MS,
                MAX_STUTTER_SLICE_MS,
            ),
            stutterSeed = stutterSeed,
            alienCarrierHz = alienCarrierHz.coerceIn(
                MIN_ALIEN_CARRIER_HZ,
                MAX_ALIEN_CARRIER_HZ,
            ),
            alienWobbleHz = alienWobbleHz.coerceIn(MIN_ALIEN_WOBBLE_HZ, MAX_ALIEN_WOBBLE_HZ),
            alienTremoloHz = alienTremoloHz.coerceIn(
                MIN_ALIEN_TREMOLO_HZ,
                MAX_ALIEN_TREMOLO_HZ,
            ),
            drunkMaxDelayMs = drunkMaxDelayMs.coerceIn(
                MIN_DRUNK_MAX_DELAY_MS,
                MAX_DRUNK_MAX_DELAY_MS,
            ),
            drunkWobbleHz = drunkWobbleHz.coerceIn(MIN_DRUNK_WOBBLE_HZ, MAX_DRUNK_WOBBLE_HZ),
            drunkTremoloHz = drunkTremoloHz.coerceIn(
                MIN_DRUNK_TREMOLO_HZ,
                MAX_DRUNK_TREMOLO_HZ,
            ),
            telephoneLowHz = telephoneLow,
            telephoneHighHz = telephoneHigh,
            telephoneDrive = telephoneDrive.coerceIn(
                MIN_TELEPHONE_DRIVE,
                MAX_TELEPHONE_DRIVE,
            ),
            megaphoneLowHz = megaphoneLow,
            megaphoneHighHz = megaphoneHigh,
            megaphoneDrive = megaphoneDrive.coerceIn(
                MIN_MEGAPHONE_DRIVE,
                MAX_MEGAPHONE_DRIVE,
            ),
            beeBuzzHz = beeBuzzHz.coerceIn(MIN_BEE_BUZZ_HZ, MAX_BEE_BUZZ_HZ),
            beeBuzzRatio = beeBuzzRatio.coerceIn(MIN_BEE_BUZZ_RATIO, MAX_BEE_BUZZ_RATIO),
            beeFlutterHz = beeFlutterHz.coerceIn(MIN_BEE_FLUTTER_HZ, MAX_BEE_FLUTTER_HZ),
            compressorMaxRatio = compressorMaxRatio.coerceIn(
                MIN_COMPRESSOR_MAX_RATIO,
                MAX_COMPRESSOR_MAX_RATIO,
            ),
            compressorThreshold = compressorThreshold.coerceIn(
                MIN_COMPRESSOR_THRESHOLD,
                MAX_COMPRESSOR_THRESHOLD,
            ),
            compressorAttackMs = compressorAttackMs.coerceIn(
                MIN_COMPRESSOR_ATTACK_MS,
                MAX_COMPRESSOR_ATTACK_MS,
            ),
            compressorReleaseMs = compressorReleaseMs.coerceIn(
                MIN_COMPRESSOR_RELEASE_MS,
                MAX_COMPRESSOR_RELEASE_MS,
            ),
            gateAttackMs = gateAttackMs.coerceIn(MIN_GATE_ATTACK_MS, MAX_GATE_ATTACK_MS),
            gateReleaseMs = gateReleaseMs.coerceIn(MIN_GATE_RELEASE_MS, MAX_GATE_RELEASE_MS),
            robotCarrierHz = robotCarrierHz.coerceIn(
                MIN_ROBOT_CARRIER_HZ,
                MAX_ROBOT_CARRIER_HZ,
            ),
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

    fun sanitized(): VoiceSettings {
        val safeTone = toneSemitones.coerceIn(MIN_TONE, MAX_TONE)
        val telephoneLow = telephoneLowHz.coerceIn(MIN_TELEPHONE_LOW_HZ, MAX_TELEPHONE_LOW_HZ)
        val telephoneHigh = telephoneHighHz
            .coerceIn(MIN_TELEPHONE_HIGH_HZ, MAX_TELEPHONE_HIGH_HZ)
            .coerceAtLeast(telephoneLow + 1.0)
        val megaphoneLow = megaphoneLowHz.coerceIn(MIN_MEGAPHONE_LOW_HZ, MAX_MEGAPHONE_LOW_HZ)
        val megaphoneHigh = megaphoneHighHz
            .coerceIn(MIN_MEGAPHONE_HIGH_HZ, MAX_MEGAPHONE_HIGH_HZ)
            .coerceAtLeast(megaphoneLow + 1.0)
        return copy(
            toneSemitones = safeTone,
            formantSemitones = sanitizeFormant(safeTone, formantSemitones),
            speed = speed.coerceIn(MIN_SPEED, MAX_SPEED),
            grit = grit.coerceIn(MIN_EFFECT, MAX_EFFECT),
            robot = robot.coerceIn(MIN_EFFECT, MAX_EFFECT),
            echo = echo.coerceIn(MIN_EFFECT, MAX_EFFECT),
            fan = fan.coerceIn(MIN_EFFECT, MAX_EFFECT),
            stutter = stutter.coerceIn(MIN_EFFECT, MAX_EFFECT),
            alien = alien.coerceIn(MIN_EFFECT, MAX_EFFECT),
            drunk = drunk.coerceIn(MIN_EFFECT, MAX_EFFECT),
            telephone = telephone.coerceIn(MIN_EFFECT, MAX_EFFECT),
            megaphone = megaphone.coerceIn(MIN_EFFECT, MAX_EFFECT),
            bee = bee.coerceIn(MIN_EFFECT, MAX_EFFECT),
            gritDrive = gritDrive.coerceIn(MIN_GRIT_DRIVE, MAX_GRIT_DRIVE),
            echoDelayMs = echoDelayMs.coerceIn(MIN_ECHO_DELAY_MS, MAX_ECHO_DELAY_MS),
            echoWet = echoWet.coerceIn(MIN_ECHO_WET, MAX_ECHO_WET),
            echoFeedback = echoFeedback.coerceIn(MIN_ECHO_FEEDBACK, MAX_ECHO_FEEDBACK),
            fanCutoffHz = fanCutoffHz.coerceIn(MIN_FAN_CUTOFF_HZ, MAX_FAN_CUTOFF_HZ),
            fanModulationHz = fanModulationHz.coerceIn(
                MIN_FAN_MODULATION_HZ,
                MAX_FAN_MODULATION_HZ,
            ),
            fanNoiseLevel = fanNoiseLevel.coerceIn(MIN_FAN_NOISE_LEVEL, MAX_FAN_NOISE_LEVEL),
            stutterSliceMs = stutterSliceMs.coerceIn(
                MIN_STUTTER_SLICE_MS,
                MAX_STUTTER_SLICE_MS,
            ),
            alienCarrierHz = alienCarrierHz.coerceIn(
                MIN_ALIEN_CARRIER_HZ,
                MAX_ALIEN_CARRIER_HZ,
            ),
            alienWobbleHz = alienWobbleHz.coerceIn(MIN_ALIEN_WOBBLE_HZ, MAX_ALIEN_WOBBLE_HZ),
            alienTremoloHz = alienTremoloHz.coerceIn(
                MIN_ALIEN_TREMOLO_HZ,
                MAX_ALIEN_TREMOLO_HZ,
            ),
            drunkMaxDelayMs = drunkMaxDelayMs.coerceIn(
                MIN_DRUNK_MAX_DELAY_MS,
                MAX_DRUNK_MAX_DELAY_MS,
            ),
            drunkWobbleHz = drunkWobbleHz.coerceIn(MIN_DRUNK_WOBBLE_HZ, MAX_DRUNK_WOBBLE_HZ),
            drunkTremoloHz = drunkTremoloHz.coerceIn(
                MIN_DRUNK_TREMOLO_HZ,
                MAX_DRUNK_TREMOLO_HZ,
            ),
            telephoneLowHz = telephoneLow,
            telephoneHighHz = telephoneHigh,
            telephoneDrive = telephoneDrive.coerceIn(
                MIN_TELEPHONE_DRIVE,
                MAX_TELEPHONE_DRIVE,
            ),
            megaphoneLowHz = megaphoneLow,
            megaphoneHighHz = megaphoneHigh,
            megaphoneDrive = megaphoneDrive.coerceIn(
                MIN_MEGAPHONE_DRIVE,
                MAX_MEGAPHONE_DRIVE,
            ),
            beeBuzzHz = beeBuzzHz.coerceIn(MIN_BEE_BUZZ_HZ, MAX_BEE_BUZZ_HZ),
            beeBuzzRatio = beeBuzzRatio.coerceIn(MIN_BEE_BUZZ_RATIO, MAX_BEE_BUZZ_RATIO),
            beeFlutterHz = beeFlutterHz.coerceIn(MIN_BEE_FLUTTER_HZ, MAX_BEE_FLUTTER_HZ),
            compressorMaxRatio = compressorMaxRatio.coerceIn(
                MIN_COMPRESSOR_MAX_RATIO,
                MAX_COMPRESSOR_MAX_RATIO,
            ),
            compressorThreshold = compressorThreshold.coerceIn(
                MIN_COMPRESSOR_THRESHOLD,
                MAX_COMPRESSOR_THRESHOLD,
            ),
            compressorAttackMs = compressorAttackMs.coerceIn(
                MIN_COMPRESSOR_ATTACK_MS,
                MAX_COMPRESSOR_ATTACK_MS,
            ),
            compressorReleaseMs = compressorReleaseMs.coerceIn(
                MIN_COMPRESSOR_RELEASE_MS,
                MAX_COMPRESSOR_RELEASE_MS,
            ),
            gateAttackMs = gateAttackMs.coerceIn(MIN_GATE_ATTACK_MS, MAX_GATE_ATTACK_MS),
            gateReleaseMs = gateReleaseMs.coerceIn(MIN_GATE_RELEASE_MS, MAX_GATE_RELEASE_MS),
            robotCarrierHz = robotCarrierHz.coerceIn(
                MIN_ROBOT_CARRIER_HZ,
                MAX_ROBOT_CARRIER_HZ,
            ),
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
            formantFftSize = normalizeFftSize(formantFftSize),
            formantOverlap = normalizeFormantOverlap(formantOverlap),
            pitchFftSize = normalizeFftSize(pitchFftSize),
            pitchOverlap = normalizePitchOverlap(pitchOverlap),
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

    fun withGritDrive(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        gritDrive = value.coerceIn(MIN_GRIT_DRIVE, MAX_GRIT_DRIVE),
    )

    fun withEchoDelay(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        echoDelayMs = value.coerceIn(MIN_ECHO_DELAY_MS, MAX_ECHO_DELAY_MS),
    )

    fun withEchoWet(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        echoWet = value.coerceIn(MIN_ECHO_WET, MAX_ECHO_WET),
    )

    fun withEchoFeedback(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        echoFeedback = value.coerceIn(MIN_ECHO_FEEDBACK, MAX_ECHO_FEEDBACK),
    )

    fun withFanCutoff(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        fanCutoffHz = value.coerceIn(MIN_FAN_CUTOFF_HZ, MAX_FAN_CUTOFF_HZ),
    )

    fun withFanModulation(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        fanModulationHz = value.coerceIn(MIN_FAN_MODULATION_HZ, MAX_FAN_MODULATION_HZ),
    )

    fun withFanNoiseLevel(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        fanNoiseLevel = value.coerceIn(MIN_FAN_NOISE_LEVEL, MAX_FAN_NOISE_LEVEL),
    )

    fun withFanNoiseSeed(value: Int): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        fanNoiseSeed = value,
    )

    fun withStutterSlice(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        stutterSliceMs = value.coerceIn(MIN_STUTTER_SLICE_MS, MAX_STUTTER_SLICE_MS),
    )

    fun withStutterSeed(value: Int): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        stutterSeed = value,
    )

    fun withAlienCarrier(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        alienCarrierHz = value.coerceIn(MIN_ALIEN_CARRIER_HZ, MAX_ALIEN_CARRIER_HZ),
    )

    fun withAlienWobble(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        alienWobbleHz = value.coerceIn(MIN_ALIEN_WOBBLE_HZ, MAX_ALIEN_WOBBLE_HZ),
    )

    fun withAlienTremolo(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        alienTremoloHz = value.coerceIn(MIN_ALIEN_TREMOLO_HZ, MAX_ALIEN_TREMOLO_HZ),
    )

    fun withDrunkMaxDelay(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        drunkMaxDelayMs = value.coerceIn(MIN_DRUNK_MAX_DELAY_MS, MAX_DRUNK_MAX_DELAY_MS),
    )

    fun withDrunkWobble(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        drunkWobbleHz = value.coerceIn(MIN_DRUNK_WOBBLE_HZ, MAX_DRUNK_WOBBLE_HZ),
    )

    fun withDrunkTremolo(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        drunkTremoloHz = value.coerceIn(MIN_DRUNK_TREMOLO_HZ, MAX_DRUNK_TREMOLO_HZ),
    )

    fun withTelephoneLow(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        telephoneLowHz = value.coerceIn(MIN_TELEPHONE_LOW_HZ, MAX_TELEPHONE_LOW_HZ),
    )

    fun withTelephoneHigh(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        telephoneHighHz = value.coerceIn(MIN_TELEPHONE_HIGH_HZ, MAX_TELEPHONE_HIGH_HZ),
    )

    fun withTelephoneDrive(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        telephoneDrive = value.coerceIn(MIN_TELEPHONE_DRIVE, MAX_TELEPHONE_DRIVE),
    )

    fun withMegaphoneLow(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        megaphoneLowHz = value.coerceIn(MIN_MEGAPHONE_LOW_HZ, MAX_MEGAPHONE_LOW_HZ),
    )

    fun withMegaphoneHigh(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        megaphoneHighHz = value.coerceIn(MIN_MEGAPHONE_HIGH_HZ, MAX_MEGAPHONE_HIGH_HZ),
    )

    fun withMegaphoneDrive(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        megaphoneDrive = value.coerceIn(MIN_MEGAPHONE_DRIVE, MAX_MEGAPHONE_DRIVE),
    )

    fun withBeeBuzz(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        beeBuzzHz = value.coerceIn(MIN_BEE_BUZZ_HZ, MAX_BEE_BUZZ_HZ),
    )

    fun withBeeBuzzRatio(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        beeBuzzRatio = value.coerceIn(MIN_BEE_BUZZ_RATIO, MAX_BEE_BUZZ_RATIO),
    )

    fun withBeeFlutter(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        beeFlutterHz = value.coerceIn(MIN_BEE_FLUTTER_HZ, MAX_BEE_FLUTTER_HZ),
    )

    fun withCompressorMaxRatio(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        compressorMaxRatio = value.coerceIn(
            MIN_COMPRESSOR_MAX_RATIO,
            MAX_COMPRESSOR_MAX_RATIO,
        ),
    )

    fun withCompressorThreshold(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        compressorThreshold = value.coerceIn(
            MIN_COMPRESSOR_THRESHOLD,
            MAX_COMPRESSOR_THRESHOLD,
        ),
    )

    fun withCompressorAttack(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        compressorAttackMs = value.coerceIn(
            MIN_COMPRESSOR_ATTACK_MS,
            MAX_COMPRESSOR_ATTACK_MS,
        ),
    )

    fun withCompressorRelease(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        compressorReleaseMs = value.coerceIn(
            MIN_COMPRESSOR_RELEASE_MS,
            MAX_COMPRESSOR_RELEASE_MS,
        ),
    )

    fun withGateAttack(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        gateAttackMs = value.coerceIn(MIN_GATE_ATTACK_MS, MAX_GATE_ATTACK_MS),
    )

    fun withGateRelease(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        gateReleaseMs = value.coerceIn(MIN_GATE_RELEASE_MS, MAX_GATE_RELEASE_MS),
    )

    fun withRobotCarrier(value: Double): VoiceSettings = copy(
        preset = VoicePreset.CUSTOM,
        robotCarrierHz = value.coerceIn(MIN_ROBOT_CARRIER_HZ, MAX_ROBOT_CARRIER_HZ),
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

        const val MIN_GRIT_DRIVE = 1.0
        const val MAX_GRIT_DRIVE = 10.0
        const val MIN_ECHO_DELAY_MS = 20.0
        const val MAX_ECHO_DELAY_MS = 1000.0
        const val MIN_ECHO_WET = 0.0
        const val MAX_ECHO_WET = 1.0
        const val MIN_ECHO_FEEDBACK = 0.0
        const val MAX_ECHO_FEEDBACK = 0.95
        const val MIN_FAN_CUTOFF_HZ = 100.0
        const val MAX_FAN_CUTOFF_HZ = 8000.0
        const val MIN_FAN_MODULATION_HZ = 0.0
        const val MAX_FAN_MODULATION_HZ = 5.0
        const val MIN_FAN_NOISE_LEVEL = 0.0
        const val MAX_FAN_NOISE_LEVEL = 1.0
        const val MIN_STUTTER_SLICE_MS = 20.0
        const val MAX_STUTTER_SLICE_MS = 500.0
        const val MIN_ALIEN_CARRIER_HZ = 20.0
        const val MAX_ALIEN_CARRIER_HZ = 2000.0
        const val MIN_ALIEN_WOBBLE_HZ = 0.0
        const val MAX_ALIEN_WOBBLE_HZ = 10.0
        const val MIN_ALIEN_TREMOLO_HZ = 0.0
        const val MAX_ALIEN_TREMOLO_HZ = 20.0
        const val MIN_DRUNK_MAX_DELAY_MS = 0.0
        const val MAX_DRUNK_MAX_DELAY_MS = 50.0
        const val MIN_DRUNK_WOBBLE_HZ = 0.0
        const val MAX_DRUNK_WOBBLE_HZ = 10.0
        const val MIN_DRUNK_TREMOLO_HZ = 0.0
        const val MAX_DRUNK_TREMOLO_HZ = 20.0
        const val MIN_TELEPHONE_LOW_HZ = 50.0
        const val MAX_TELEPHONE_LOW_HZ = 1500.0
        const val MIN_TELEPHONE_HIGH_HZ = 1000.0
        const val MAX_TELEPHONE_HIGH_HZ = 8000.0
        const val MIN_TELEPHONE_DRIVE = 1.0
        const val MAX_TELEPHONE_DRIVE = 10.0
        const val MIN_MEGAPHONE_LOW_HZ = 50.0
        const val MAX_MEGAPHONE_LOW_HZ = 2000.0
        const val MIN_MEGAPHONE_HIGH_HZ = 1000.0
        const val MAX_MEGAPHONE_HIGH_HZ = 8000.0
        const val MIN_MEGAPHONE_DRIVE = 1.0
        const val MAX_MEGAPHONE_DRIVE = 10.0
        const val MIN_BEE_BUZZ_HZ = 5.0
        const val MAX_BEE_BUZZ_HZ = 200.0
        const val MIN_BEE_BUZZ_RATIO = 1.0
        const val MAX_BEE_BUZZ_RATIO = 2.0
        const val MIN_BEE_FLUTTER_HZ = 0.0
        const val MAX_BEE_FLUTTER_HZ = 10.0
        const val MIN_COMPRESSOR_MAX_RATIO = 1.0
        const val MAX_COMPRESSOR_MAX_RATIO = 20.0
        const val MIN_COMPRESSOR_THRESHOLD = 0.01
        const val MAX_COMPRESSOR_THRESHOLD = 1.0
        const val MIN_COMPRESSOR_ATTACK_MS = 0.1
        const val MAX_COMPRESSOR_ATTACK_MS = 100.0
        const val MIN_COMPRESSOR_RELEASE_MS = 1.0
        const val MAX_COMPRESSOR_RELEASE_MS = 1000.0
        const val MIN_GATE_ATTACK_MS = 0.1
        const val MAX_GATE_ATTACK_MS = 100.0
        const val MIN_GATE_RELEASE_MS = 1.0
        const val MAX_GATE_RELEASE_MS = 1000.0
        const val MIN_ROBOT_CARRIER_HZ = 20.0
        const val MAX_ROBOT_CARRIER_HZ = 500.0

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
        const val DEFAULT_GRIT_DRIVE = 4.0
        const val DEFAULT_ECHO_DELAY_MS = 180.0
        const val DEFAULT_ECHO_WET = 0.7
        const val DEFAULT_ECHO_FEEDBACK = 0.45
        const val DEFAULT_FAN_CUTOFF_HZ = 1200.0
        const val DEFAULT_FAN_MODULATION_HZ = 0.3
        const val DEFAULT_FAN_NOISE_LEVEL = 0.18
        const val DEFAULT_FAN_NOISE_SEED = 0x12345678
        const val DEFAULT_STUTTER_SLICE_MS = 120.0
        const val DEFAULT_STUTTER_SEED = 0x6D2B79F5
        const val DEFAULT_ALIEN_CARRIER_HZ = 220.0
        const val DEFAULT_ALIEN_WOBBLE_HZ = 0.6
        const val DEFAULT_ALIEN_TREMOLO_HZ = 5.0
        const val DEFAULT_DRUNK_MAX_DELAY_MS = 15.0
        const val DEFAULT_DRUNK_WOBBLE_HZ = 0.8
        const val DEFAULT_DRUNK_TREMOLO_HZ = 4.5
        const val DEFAULT_TELEPHONE_LOW_HZ = 300.0
        const val DEFAULT_TELEPHONE_HIGH_HZ = 3400.0
        const val DEFAULT_TELEPHONE_DRIVE = 2.0
        const val DEFAULT_MEGAPHONE_LOW_HZ = 500.0
        const val DEFAULT_MEGAPHONE_HIGH_HZ = 3000.0
        const val DEFAULT_MEGAPHONE_DRIVE = 4.0
        const val DEFAULT_BEE_BUZZ_HZ = 35.0
        const val DEFAULT_BEE_BUZZ_RATIO = 1.17
        const val DEFAULT_BEE_FLUTTER_HZ = 0.7
        const val DEFAULT_COMPRESSOR_MAX_RATIO = 7.0
        const val DEFAULT_COMPRESSOR_THRESHOLD = 0.25
        const val DEFAULT_COMPRESSOR_ATTACK_MS = 5.0
        const val DEFAULT_COMPRESSOR_RELEASE_MS = 100.0
        const val DEFAULT_GATE_ATTACK_MS = 5.0
        const val DEFAULT_GATE_RELEASE_MS = 100.0
        const val DEFAULT_ROBOT_CARRIER_HZ = 80.0
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
