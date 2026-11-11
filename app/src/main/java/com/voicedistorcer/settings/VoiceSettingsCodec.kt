package com.voicedistorcer.settings

import java.io.StringReader
import java.io.StringWriter
import java.util.Properties

object VoiceSettingsCodec {
    const val FORMAT = "voice-distorcer-settings"
    const val VERSION = 1

    fun encode(settings: VoiceSettings): String {
        val safe = settings.sanitized()
        val properties = Properties().apply {
            setProperty(KEY_FORMAT, FORMAT)
            setProperty(KEY_VERSION, VERSION.toString())
            setProperty(KEY_PRESET, safe.preset.name)
            setProperty(KEY_TONE, safe.toneSemitones.toString())
            setProperty(KEY_FORMANT, safe.formantSemitones.toString())
            setProperty(KEY_SPEED, safe.speed.toString())
            setProperty(KEY_GRIT, safe.grit.toString())
            setProperty(KEY_ROBOT, safe.robot.toString())
            setProperty(KEY_ECHO, safe.echo.toString())
            setProperty(KEY_FAN, safe.fan.toString())
            setProperty(KEY_STUTTER, safe.stutter.toString())
            setProperty(KEY_REVERSE, safe.reverse.toString())
            setProperty(KEY_ALIEN, safe.alien.toString())
            setProperty(KEY_DRUNK, safe.drunk.toString())
            setProperty(KEY_TELEPHONE, safe.telephone.toString())
            setProperty(KEY_MEGAPHONE, safe.megaphone.toString())
            setProperty(KEY_BEE, safe.bee.toString())
            setProperty(KEY_GRIT_DRIVE, safe.gritDrive.toString())
            setProperty(KEY_ECHO_DELAY, safe.echoDelayMs.toString())
            setProperty(KEY_ECHO_WET, safe.echoWet.toString())
            setProperty(KEY_ECHO_FEEDBACK, safe.echoFeedback.toString())
            setProperty(KEY_FAN_CUTOFF, safe.fanCutoffHz.toString())
            setProperty(KEY_FAN_MODULATION, safe.fanModulationHz.toString())
            setProperty(KEY_FAN_NOISE_LEVEL, safe.fanNoiseLevel.toString())
            setProperty(KEY_FAN_NOISE_SEED, safe.fanNoiseSeed.toString())
            setProperty(KEY_STUTTER_SLICE, safe.stutterSliceMs.toString())
            setProperty(KEY_STUTTER_SEED, safe.stutterSeed.toString())
            setProperty(KEY_ALIEN_CARRIER, safe.alienCarrierHz.toString())
            setProperty(KEY_ALIEN_WOBBLE, safe.alienWobbleHz.toString())
            setProperty(KEY_ALIEN_TREMOLO, safe.alienTremoloHz.toString())
            setProperty(KEY_DRUNK_MAX_DELAY, safe.drunkMaxDelayMs.toString())
            setProperty(KEY_DRUNK_WOBBLE, safe.drunkWobbleHz.toString())
            setProperty(KEY_DRUNK_TREMOLO, safe.drunkTremoloHz.toString())
            setProperty(KEY_TELEPHONE_LOW, safe.telephoneLowHz.toString())
            setProperty(KEY_TELEPHONE_HIGH, safe.telephoneHighHz.toString())
            setProperty(KEY_TELEPHONE_DRIVE, safe.telephoneDrive.toString())
            setProperty(KEY_MEGAPHONE_LOW, safe.megaphoneLowHz.toString())
            setProperty(KEY_MEGAPHONE_HIGH, safe.megaphoneHighHz.toString())
            setProperty(KEY_MEGAPHONE_DRIVE, safe.megaphoneDrive.toString())
            setProperty(KEY_BEE_BUZZ, safe.beeBuzzHz.toString())
            setProperty(KEY_BEE_BUZZ_RATIO, safe.beeBuzzRatio.toString())
            setProperty(KEY_BEE_FLUTTER, safe.beeFlutterHz.toString())
            setProperty(KEY_COMPRESSOR_MAX_RATIO, safe.compressorMaxRatio.toString())
            setProperty(KEY_COMPRESSOR_THRESHOLD, safe.compressorThreshold.toString())
            setProperty(KEY_COMPRESSOR_ATTACK, safe.compressorAttackMs.toString())
            setProperty(KEY_COMPRESSOR_RELEASE, safe.compressorReleaseMs.toString())
            setProperty(KEY_GATE_ATTACK, safe.gateAttackMs.toString())
            setProperty(KEY_GATE_RELEASE, safe.gateReleaseMs.toString())
            setProperty(KEY_ROBOT_CARRIER, safe.robotCarrierHz.toString())
            setProperty(KEY_HIGH_PASS, safe.highPassHz.toString())
            setProperty(KEY_NOISE_GATE, safe.noiseGateDb.toString())
            setProperty(KEY_COMPRESSOR, safe.compressor.toString())
            setProperty(KEY_OUTPUT_GAIN, safe.outputGainDb.toString())
            setProperty(KEY_FORMANT_MIX, safe.formantMix.toString())
            setProperty(KEY_SPECTRAL_SMOOTHING, safe.spectralSmoothing.toString())
            setProperty(KEY_FORMANT_GAIN_LIMIT, safe.formantGainLimit.toString())
            setProperty(KEY_FORMANT_FFT, safe.formantFftSize.toString())
            setProperty(KEY_FORMANT_OVERLAP, safe.formantOverlap.toString())
            setProperty(KEY_PITCH_FFT, safe.pitchFftSize.toString())
            setProperty(KEY_PITCH_OVERLAP, safe.pitchOverlap.toString())
        }
        val writer = StringWriter()
        properties.store(writer, null)
        val body = writer.toString()
            .lineSequence()
            .filterNot { it.startsWith("#") }
            .filter { it.isNotBlank() }
            .sorted()
            .joinToString(separator = "\n", postfix = "\n")
        return "# Voice Distorcer settings\n# Format version $VERSION\n$body"
    }

    fun decode(text: String): VoiceSettings {
        val properties = Properties()
        properties.load(StringReader(text))
        require(properties.getProperty(KEY_FORMAT) == FORMAT) {
            "Unsupported settings format"
        }
        val version = properties.getProperty(KEY_VERSION)?.toIntOrNull()
        require(version != null && version in 1..VERSION) {
            "Unsupported settings version"
        }
        val preset = properties.getProperty(KEY_PRESET)
            ?.let { runCatching { VoicePreset.valueOf(it) }.getOrNull() }
            ?: VoicePreset.CUSTOM
        val fallback = VoiceSettings.forPreset(
            if (preset == VoicePreset.CUSTOM) VoicePreset.DEFAULT else preset,
        )
        return VoiceSettings(
            preset = preset,
            toneSemitones = properties.double(KEY_TONE, fallback.toneSemitones),
            formantSemitones = properties.double(KEY_FORMANT, fallback.formantSemitones),
            speed = properties.double(KEY_SPEED, fallback.speed),
            grit = properties.double(KEY_GRIT, fallback.grit),
            robot = properties.double(KEY_ROBOT, fallback.robot),
            echo = properties.double(KEY_ECHO, fallback.echo),
            fan = properties.double(KEY_FAN, fallback.fan),
            stutter = properties.double(KEY_STUTTER, fallback.stutter),
            reverse = properties.boolean(KEY_REVERSE, fallback.reverse),
            alien = properties.double(KEY_ALIEN, fallback.alien),
            drunk = properties.double(KEY_DRUNK, fallback.drunk),
            telephone = properties.double(KEY_TELEPHONE, fallback.telephone),
            megaphone = properties.double(KEY_MEGAPHONE, fallback.megaphone),
            bee = properties.double(KEY_BEE, fallback.bee),
            gritDrive = properties.double(KEY_GRIT_DRIVE, fallback.gritDrive),
            echoDelayMs = properties.double(KEY_ECHO_DELAY, fallback.echoDelayMs),
            echoWet = properties.double(KEY_ECHO_WET, fallback.echoWet),
            echoFeedback = properties.double(KEY_ECHO_FEEDBACK, fallback.echoFeedback),
            fanCutoffHz = properties.double(KEY_FAN_CUTOFF, fallback.fanCutoffHz),
            fanModulationHz = properties.double(KEY_FAN_MODULATION, fallback.fanModulationHz),
            fanNoiseLevel = properties.double(KEY_FAN_NOISE_LEVEL, fallback.fanNoiseLevel),
            fanNoiseSeed = properties.int(KEY_FAN_NOISE_SEED, fallback.fanNoiseSeed),
            stutterSliceMs = properties.double(KEY_STUTTER_SLICE, fallback.stutterSliceMs),
            stutterSeed = properties.int(KEY_STUTTER_SEED, fallback.stutterSeed),
            alienCarrierHz = properties.double(KEY_ALIEN_CARRIER, fallback.alienCarrierHz),
            alienWobbleHz = properties.double(KEY_ALIEN_WOBBLE, fallback.alienWobbleHz),
            alienTremoloHz = properties.double(KEY_ALIEN_TREMOLO, fallback.alienTremoloHz),
            drunkMaxDelayMs = properties.double(KEY_DRUNK_MAX_DELAY, fallback.drunkMaxDelayMs),
            drunkWobbleHz = properties.double(KEY_DRUNK_WOBBLE, fallback.drunkWobbleHz),
            drunkTremoloHz = properties.double(KEY_DRUNK_TREMOLO, fallback.drunkTremoloHz),
            telephoneLowHz = properties.double(KEY_TELEPHONE_LOW, fallback.telephoneLowHz),
            telephoneHighHz = properties.double(KEY_TELEPHONE_HIGH, fallback.telephoneHighHz),
            telephoneDrive = properties.double(KEY_TELEPHONE_DRIVE, fallback.telephoneDrive),
            megaphoneLowHz = properties.double(KEY_MEGAPHONE_LOW, fallback.megaphoneLowHz),
            megaphoneHighHz = properties.double(KEY_MEGAPHONE_HIGH, fallback.megaphoneHighHz),
            megaphoneDrive = properties.double(KEY_MEGAPHONE_DRIVE, fallback.megaphoneDrive),
            beeBuzzHz = properties.double(KEY_BEE_BUZZ, fallback.beeBuzzHz),
            beeBuzzRatio = properties.double(KEY_BEE_BUZZ_RATIO, fallback.beeBuzzRatio),
            beeFlutterHz = properties.double(KEY_BEE_FLUTTER, fallback.beeFlutterHz),
            compressorMaxRatio = properties.double(
                KEY_COMPRESSOR_MAX_RATIO,
                fallback.compressorMaxRatio,
            ),
            compressorThreshold = properties.double(
                KEY_COMPRESSOR_THRESHOLD,
                fallback.compressorThreshold,
            ),
            compressorAttackMs = properties.double(
                KEY_COMPRESSOR_ATTACK,
                fallback.compressorAttackMs,
            ),
            compressorReleaseMs = properties.double(
                KEY_COMPRESSOR_RELEASE,
                fallback.compressorReleaseMs,
            ),
            gateAttackMs = properties.double(KEY_GATE_ATTACK, fallback.gateAttackMs),
            gateReleaseMs = properties.double(KEY_GATE_RELEASE, fallback.gateReleaseMs),
            robotCarrierHz = properties.double(KEY_ROBOT_CARRIER, fallback.robotCarrierHz),
            highPassHz = properties.double(KEY_HIGH_PASS, fallback.highPassHz),
            noiseGateDb = properties.double(KEY_NOISE_GATE, fallback.noiseGateDb),
            compressor = properties.double(KEY_COMPRESSOR, fallback.compressor),
            outputGainDb = properties.double(KEY_OUTPUT_GAIN, fallback.outputGainDb),
            formantMix = properties.double(KEY_FORMANT_MIX, fallback.formantMix),
            spectralSmoothing = properties.double(
                KEY_SPECTRAL_SMOOTHING,
                fallback.spectralSmoothing,
            ),
            formantGainLimit = properties.double(
                KEY_FORMANT_GAIN_LIMIT,
                fallback.formantGainLimit,
            ),
            formantFftSize = properties.int(KEY_FORMANT_FFT, fallback.formantFftSize),
            formantOverlap = properties.int(KEY_FORMANT_OVERLAP, fallback.formantOverlap),
            pitchFftSize = properties.int(KEY_PITCH_FFT, fallback.pitchFftSize),
            pitchOverlap = properties.int(KEY_PITCH_OVERLAP, fallback.pitchOverlap),
        ).sanitized()
    }

    private fun Properties.double(key: String, fallback: Double): Double =
        getProperty(key)?.toDoubleOrNull() ?: fallback

    private fun Properties.int(key: String, fallback: Int): Int =
        getProperty(key)?.toIntOrNull() ?: fallback

    private fun Properties.boolean(key: String, fallback: Boolean): Boolean =
        getProperty(key)?.toBooleanStrictOrNull() ?: fallback

    private const val KEY_FORMAT = "format"
    private const val KEY_VERSION = "version"
    private const val KEY_PRESET = "preset"
    private const val KEY_TONE = "toneSemitones"
    private const val KEY_FORMANT = "formantSemitones"
    private const val KEY_SPEED = "speed"
    private const val KEY_GRIT = "grit"
    private const val KEY_ROBOT = "robot"
    private const val KEY_ECHO = "echo"
    private const val KEY_FAN = "fan"
    private const val KEY_STUTTER = "stutter"
    private const val KEY_REVERSE = "reverse"
    private const val KEY_ALIEN = "alien"
    private const val KEY_DRUNK = "drunk"
    private const val KEY_TELEPHONE = "telephone"
    private const val KEY_MEGAPHONE = "megaphone"
    private const val KEY_BEE = "bee"
    private const val KEY_GRIT_DRIVE = "gritDrive"
    private const val KEY_ECHO_DELAY = "echoDelayMs"
    private const val KEY_ECHO_WET = "echoWet"
    private const val KEY_ECHO_FEEDBACK = "echoFeedback"
    private const val KEY_FAN_CUTOFF = "fanCutoffHz"
    private const val KEY_FAN_MODULATION = "fanModulationHz"
    private const val KEY_FAN_NOISE_LEVEL = "fanNoiseLevel"
    private const val KEY_FAN_NOISE_SEED = "fanNoiseSeed"
    private const val KEY_STUTTER_SLICE = "stutterSliceMs"
    private const val KEY_STUTTER_SEED = "stutterSeed"
    private const val KEY_ALIEN_CARRIER = "alienCarrierHz"
    private const val KEY_ALIEN_WOBBLE = "alienWobbleHz"
    private const val KEY_ALIEN_TREMOLO = "alienTremoloHz"
    private const val KEY_DRUNK_MAX_DELAY = "drunkMaxDelayMs"
    private const val KEY_DRUNK_WOBBLE = "drunkWobbleHz"
    private const val KEY_DRUNK_TREMOLO = "drunkTremoloHz"
    private const val KEY_TELEPHONE_LOW = "telephoneLowHz"
    private const val KEY_TELEPHONE_HIGH = "telephoneHighHz"
    private const val KEY_TELEPHONE_DRIVE = "telephoneDrive"
    private const val KEY_MEGAPHONE_LOW = "megaphoneLowHz"
    private const val KEY_MEGAPHONE_HIGH = "megaphoneHighHz"
    private const val KEY_MEGAPHONE_DRIVE = "megaphoneDrive"
    private const val KEY_BEE_BUZZ = "beeBuzzHz"
    private const val KEY_BEE_BUZZ_RATIO = "beeBuzzRatio"
    private const val KEY_BEE_FLUTTER = "beeFlutterHz"
    private const val KEY_COMPRESSOR_MAX_RATIO = "compressorMaxRatio"
    private const val KEY_COMPRESSOR_THRESHOLD = "compressorThreshold"
    private const val KEY_COMPRESSOR_ATTACK = "compressorAttackMs"
    private const val KEY_COMPRESSOR_RELEASE = "compressorReleaseMs"
    private const val KEY_GATE_ATTACK = "gateAttackMs"
    private const val KEY_GATE_RELEASE = "gateReleaseMs"
    private const val KEY_ROBOT_CARRIER = "robotCarrierHz"
    private const val KEY_HIGH_PASS = "highPassHz"
    private const val KEY_NOISE_GATE = "noiseGateDb"
    private const val KEY_COMPRESSOR = "compressor"
    private const val KEY_OUTPUT_GAIN = "outputGainDb"
    private const val KEY_FORMANT_MIX = "formantMix"
    private const val KEY_SPECTRAL_SMOOTHING = "spectralSmoothing"
    private const val KEY_FORMANT_GAIN_LIMIT = "formantGainLimit"
    private const val KEY_FORMANT_FFT = "formantFftSize"
    private const val KEY_FORMANT_OVERLAP = "formantOverlap"
    private const val KEY_PITCH_FFT = "pitchFftSize"
    private const val KEY_PITCH_OVERLAP = "pitchOverlap"
}
