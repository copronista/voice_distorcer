package com.voicedistorcer.processing

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tanh

data class DistortionConfig(
    val pitchSemitones: Double = -4.0,
    val formantSemitones: Double = 2.0,
    val speed: Double = 1.0,
    val grit: Double = 0.0,
    val robot: Double = 0.0,
    val echo: Double = 0.0,
    val fan: Double = 0.0,
    val stutter: Double = 0.0,
    val reverse: Boolean = false,
    val alien: Double = 0.0,
    val drunk: Double = 0.0,
    val telephone: Double = 0.0,
    val megaphone: Double = 0.0,
    val bee: Double = 0.0,
    val gritDrive: Double = 4.0,
    val echoDelayMs: Double = 180.0,
    val echoWet: Double = 0.7,
    val echoFeedback: Double = 0.45,
    val fanCutoffHz: Double = 1200.0,
    val fanModulationHz: Double = 0.3,
    val fanNoiseLevel: Double = 0.18,
    val fanNoiseSeed: Int = 0x12345678,
    val stutterSliceMs: Double = 120.0,
    val stutterSeed: Int = 0x6D2B79F5,
    val alienCarrierHz: Double = 220.0,
    val alienWobbleHz: Double = 0.6,
    val alienTremoloHz: Double = 5.0,
    val drunkMaxDelayMs: Double = 15.0,
    val drunkWobbleHz: Double = 0.8,
    val drunkTremoloHz: Double = 4.5,
    val telephoneLowHz: Double = 300.0,
    val telephoneHighHz: Double = 3400.0,
    val telephoneDrive: Double = 2.0,
    val megaphoneLowHz: Double = 500.0,
    val megaphoneHighHz: Double = 3000.0,
    val megaphoneDrive: Double = 4.0,
    val beeBuzzHz: Double = 35.0,
    val beeBuzzRatio: Double = 1.17,
    val beeFlutterHz: Double = 0.7,
    val compressorMaxRatio: Double = 7.0,
    val compressorThreshold: Double = 0.25,
    val compressorAttackMs: Double = 5.0,
    val compressorReleaseMs: Double = 100.0,
    val gateAttackMs: Double = 5.0,
    val gateReleaseMs: Double = 100.0,
    val robotCarrierHz: Double = 80.0,
    val highPassHz: Double = 0.0,
    val noiseGateDb: Double = -80.0,
    val compressor: Double = 0.0,
    val outputGainDb: Double = 0.0,
    val formantMix: Double = 1.0,
    val spectralSmoothing: Double = 0.5,
    val formantGainLimit: Double = 4.0,
    val formantFftSize: Int = 2048,
    val formantHopSize: Int = 512,
    val pitchFftSize: Int = 2048,
    val pitchHopSize: Int = 512,
)

class VoiceDistortionProcessor(
    private val config: DistortionConfig = DistortionConfig(),
) {
    private val pitchShifter = PitchShifter(
        PhaseVocoder(config.pitchFftSize, config.pitchHopSize),
    )
    private val formantShifter = FormantShifter(
        fftSize = config.formantFftSize,
        hopSize = config.formantHopSize,
        gainLimit = config.formantGainLimit,
        smoothing = config.spectralSmoothing,
    )
    private val tempoShifter = PhaseVocoder()

    fun process(input: ShortArray, sampleRate: Int = DEFAULT_SAMPLE_RATE): ShortArray {
        if (input.isEmpty()) return ShortArray(0)

        var processed = DoubleArray(input.size) { index -> input[index] / 32768.0 }

        val highPass = config.highPassHz.coerceIn(MIN_HIGH_PASS_HZ, MAX_HIGH_PASS_HZ)
        if (highPass > 0.0) {
            processed = applyHighPass(processed, sampleRate, highPass)
        }

        val gateDb = config.noiseGateDb.coerceIn(MIN_NOISE_GATE_DB, MAX_NOISE_GATE_DB)
        if (gateDb > MIN_NOISE_GATE_DB) {
            val gateAttack = config.gateAttackMs
                .coerceIn(MIN_GATE_ATTACK_MS, MAX_GATE_ATTACK_MS) / 1000.0
            val gateRelease = config.gateReleaseMs
                .coerceIn(MIN_GATE_RELEASE_MS, MAX_GATE_RELEASE_MS) / 1000.0
            processed = applyNoiseGate(processed, sampleRate, gateDb, gateAttack, gateRelease)
        }

        val formantMix = config.formantMix.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (config.formantSemitones != 0.0 && formantMix > 0.0) {
            val shifted = formantShifter.shift(processed, config.formantSemitones)
            processed = blend(processed, shifted, formantMix)
        }
        if (config.pitchSemitones != 0.0) {
            processed = pitchShifter.shift(processed, config.pitchSemitones)
        }

        val speed = config.speed.coerceIn(MIN_SPEED, MAX_SPEED)
        if (speed != 1.0 && processed.isNotEmpty()) {
            processed = tempoShifter.timeStretch(processed, 1.0 / speed)
        }

        val robot = config.robot.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (robot > 0.0 && processed.isNotEmpty()) {
            val robotCarrier = config.robotCarrierHz
                .coerceIn(MIN_ROBOT_CARRIER_HZ, MAX_ROBOT_CARRIER_HZ)
            processed = applyRobot(processed, robot, sampleRate, robotCarrier)
        }

        val alien = config.alien.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (alien > 0.0 && processed.isNotEmpty()) {
            val alienCarrier = config.alienCarrierHz
                .coerceIn(MIN_ALIEN_CARRIER_HZ, MAX_ALIEN_CARRIER_HZ)
            val alienWobble = config.alienWobbleHz
                .coerceIn(MIN_ALIEN_WOBBLE_HZ, MAX_ALIEN_WOBBLE_HZ)
            val alienTremolo = config.alienTremoloHz
                .coerceIn(MIN_ALIEN_TREMOLO_HZ, MAX_ALIEN_TREMOLO_HZ)
            processed = applyAlien(
                processed,
                sampleRate,
                alien,
                alienCarrier,
                alienWobble,
                alienTremolo,
            )
        }

        val drunk = config.drunk.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (drunk > 0.0 && processed.isNotEmpty()) {
            val drunkMaxDelay = config.drunkMaxDelayMs
                .coerceIn(MIN_DRUNK_MAX_DELAY_MS, MAX_DRUNK_MAX_DELAY_MS)
            val drunkWobble = config.drunkWobbleHz
                .coerceIn(MIN_DRUNK_WOBBLE_HZ, MAX_DRUNK_WOBBLE_HZ)
            val drunkTremolo = config.drunkTremoloHz
                .coerceIn(MIN_DRUNK_TREMOLO_HZ, MAX_DRUNK_TREMOLO_HZ)
            processed = applyDrunk(
                processed,
                sampleRate,
                drunk,
                drunkMaxDelay,
                drunkWobble,
                drunkTremolo,
            )
        }

        val grit = config.grit.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (grit > 0.0 && processed.isNotEmpty()) {
            val gritDrive = config.gritDrive.coerceIn(MIN_GRIT_DRIVE, MAX_GRIT_DRIVE)
            processed = applyGrit(processed, grit, gritDrive)
        }

        val compressor = config.compressor.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (compressor > 0.0 && processed.isNotEmpty()) {
            val compressorMaxRatio = config.compressorMaxRatio
                .coerceIn(MIN_COMPRESSOR_MAX_RATIO, MAX_COMPRESSOR_MAX_RATIO)
            val compressorThreshold = config.compressorThreshold
                .coerceIn(MIN_COMPRESSOR_THRESHOLD, MAX_COMPRESSOR_THRESHOLD)
            val compressorAttack = config.compressorAttackMs
                .coerceIn(MIN_COMPRESSOR_ATTACK_MS, MAX_COMPRESSOR_ATTACK_MS) / 1000.0
            val compressorRelease = config.compressorReleaseMs
                .coerceIn(MIN_COMPRESSOR_RELEASE_MS, MAX_COMPRESSOR_RELEASE_MS) / 1000.0
            processed = applyCompressor(
                processed,
                sampleRate,
                compressor,
                compressorMaxRatio,
                compressorThreshold,
                compressorAttack,
                compressorRelease,
            )
        }

        if (config.reverse && processed.isNotEmpty()) {
            processed = processed.reversedArray()
        }

        val stutter = config.stutter.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (stutter > 0.0 && processed.isNotEmpty()) {
            val stutterSlice = config.stutterSliceMs
                .coerceIn(MIN_STUTTER_SLICE_MS, MAX_STUTTER_SLICE_MS)
            processed = applyStutter(
                processed,
                sampleRate,
                stutter,
                stutterSlice,
                config.stutterSeed,
            )
        }

        val telephone = config.telephone.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (telephone > 0.0 && processed.isNotEmpty()) {
            val telephoneLow = config.telephoneLowHz
                .coerceIn(MIN_TELEPHONE_LOW_HZ, MAX_TELEPHONE_LOW_HZ)
            val telephoneHigh = config.telephoneHighHz
                .coerceIn(MIN_TELEPHONE_HIGH_HZ, MAX_TELEPHONE_HIGH_HZ)
                .coerceAtLeast(telephoneLow + 1.0)
            val telephoneDrive = config.telephoneDrive
                .coerceIn(MIN_TELEPHONE_DRIVE, MAX_TELEPHONE_DRIVE)
            processed = applyTelephone(
                processed,
                sampleRate,
                telephone,
                telephoneLow,
                telephoneHigh,
                telephoneDrive,
            )
        }

        val megaphone = config.megaphone.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (megaphone > 0.0 && processed.isNotEmpty()) {
            val megaphoneLow = config.megaphoneLowHz
                .coerceIn(MIN_MEGAPHONE_LOW_HZ, MAX_MEGAPHONE_LOW_HZ)
            val megaphoneHigh = config.megaphoneHighHz
                .coerceIn(MIN_MEGAPHONE_HIGH_HZ, MAX_MEGAPHONE_HIGH_HZ)
                .coerceAtLeast(megaphoneLow + 1.0)
            val megaphoneDrive = config.megaphoneDrive
                .coerceIn(MIN_MEGAPHONE_DRIVE, MAX_MEGAPHONE_DRIVE)
            processed = applyMegaphone(
                processed,
                sampleRate,
                megaphone,
                megaphoneLow,
                megaphoneHigh,
                megaphoneDrive,
            )
        }

        val bee = config.bee.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (bee > 0.0 && processed.isNotEmpty()) {
            val beeBuzz = config.beeBuzzHz.coerceIn(MIN_BEE_BUZZ_HZ, MAX_BEE_BUZZ_HZ)
            val beeBuzzRatio = config.beeBuzzRatio
                .coerceIn(MIN_BEE_BUZZ_RATIO, MAX_BEE_BUZZ_RATIO)
            val beeFlutter = config.beeFlutterHz
                .coerceIn(MIN_BEE_FLUTTER_HZ, MAX_BEE_FLUTTER_HZ)
            processed = applyBee(
                processed,
                sampleRate,
                bee,
                beeBuzz,
                beeBuzzRatio,
                beeFlutter,
            )
        }

        val echo = config.echo.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (echo > 0.0 && processed.isNotEmpty()) {
            val echoDelay = config.echoDelayMs.coerceIn(MIN_ECHO_DELAY_MS, MAX_ECHO_DELAY_MS)
            val echoWet = config.echoWet.coerceIn(MIN_ECHO_WET, MAX_ECHO_WET)
            val echoFeedback = config.echoFeedback
                .coerceIn(MIN_ECHO_FEEDBACK, MAX_ECHO_FEEDBACK)
            processed = applyEcho(
                processed,
                sampleRate,
                echo,
                echoDelay,
                echoWet,
                echoFeedback,
            )
        }

        val fan = config.fan.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (fan > 0.0 && processed.isNotEmpty()) {
            val fanCutoff = config.fanCutoffHz
                .coerceIn(MIN_FAN_CUTOFF_HZ, MAX_FAN_CUTOFF_HZ)
            val fanModulation = config.fanModulationHz
                .coerceIn(MIN_FAN_MODULATION_HZ, MAX_FAN_MODULATION_HZ)
            val fanNoiseLevel = config.fanNoiseLevel
                .coerceIn(MIN_FAN_NOISE_LEVEL, MAX_FAN_NOISE_LEVEL)
            processed = applyFan(
                processed,
                sampleRate,
                fan,
                fanCutoff,
                fanModulation,
                fanNoiseLevel,
                config.fanNoiseSeed,
            )
        }

        val outputGainDb = config.outputGainDb.coerceIn(
            MIN_OUTPUT_GAIN_DB,
            MAX_OUTPUT_GAIN_DB,
        )
        if (outputGainDb != 0.0 && processed.isNotEmpty()) {
            processed = applyOutputGain(processed, outputGainDb)
        }

        if (processed.isEmpty()) return ShortArray(0)

        return ShortArray(processed.size) { index ->
            (processed[index] * 32767.0).roundToInt().coerceIn(-32768, 32767).toShort()
        }
    }

    private fun blend(dry: DoubleArray, wet: DoubleArray, mix: Double): DoubleArray {
        if (mix >= 1.0) return wet
        if (mix <= 0.0) return dry
        return DoubleArray(dry.size) { index ->
            dry[index] * (1.0 - mix) + wet[index] * mix
        }
    }

    private fun applyHighPass(
        input: DoubleArray,
        sampleRate: Int,
        cutoffHz: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val dt = 1.0 / sampleRate
        val rc = 1.0 / (2.0 * PI * cutoffHz)
        val alpha = rc / (rc + dt)
        val output = DoubleArray(input.size)
        var previousInput = input[0]
        var previousOutput = 0.0
        for (index in input.indices) {
            val current = input[index]
            previousOutput = alpha * (previousOutput + current - previousInput)
            previousInput = current
            output[index] = previousOutput
        }
        return output
    }

    private fun applyNoiseGate(
        input: DoubleArray,
        sampleRate: Int,
        thresholdDb: Double,
        attackSeconds: Double,
        releaseSeconds: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val threshold = 10.0.pow(thresholdDb / 20.0)
        val attack = exp(-1.0 / (attackSeconds * sampleRate))
        val release = exp(-1.0 / (releaseSeconds * sampleRate))
        val output = DoubleArray(input.size)
        var gain = 1.0
        for (index in input.indices) {
            val sample = input[index]
            val target = if (abs(sample) >= threshold) 1.0 else 0.0
            val coefficient = if (target > gain) attack else release
            gain = coefficient * gain + (1.0 - coefficient) * target
            output[index] = sample * gain
        }
        return output
    }

    private fun applyCompressor(
        input: DoubleArray,
        sampleRate: Int,
        amount: Double,
        maxRatio: Double,
        threshold: Double,
        attackSeconds: Double,
        releaseSeconds: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val ratio = 1.0 + amount * maxRatio
        val attack = exp(-1.0 / (attackSeconds * sampleRate))
        val release = exp(-1.0 / (releaseSeconds * sampleRate))
        val output = DoubleArray(input.size)
        var envelope = 0.0
        for (index in input.indices) {
            val sample = input[index]
            val level = abs(sample)
            val coefficient = if (level > envelope) attack else release
            envelope = coefficient * envelope + (1.0 - coefficient) * level
            val gain = if (envelope > threshold) {
                (threshold + (envelope - threshold) / ratio) / envelope
            } else {
                1.0
            }
            output[index] = sample * gain
        }
        return output
    }

    private fun applyEcho(
        input: DoubleArray,
        sampleRate: Int,
        amount: Double,
        delayMs: Double,
        maxWet: Double,
        maxFeedback: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val delaySamples = (delayMs / 1000.0 * sampleRate).roundToInt().coerceAtLeast(1)
        val delay = DoubleArray(delaySamples)
        val wet = amount * maxWet
        val feedback = amount * maxFeedback
        val output = DoubleArray(input.size)
        var index = 0
        for (sampleIndex in input.indices) {
            val dry = input[sampleIndex]
            val delayed = delay[index]
            output[sampleIndex] = dry * (1.0 - wet) + delayed * wet
            delay[index] = dry + delayed * feedback
            index = (index + 1) % delaySamples
        }
        return output
    }

    private fun applyFan(
        input: DoubleArray,
        sampleRate: Int,
        amount: Double,
        cutoffHz: Double,
        modulationHz: Double,
        noiseLevel: Double,
        seed: Int,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val output = DoubleArray(input.size)
        val smoothing = 1.0 - exp(-2.0 * PI * cutoffHz / sampleRate)
        val modulationStep = 2.0 * PI * modulationHz / sampleRate
        var noiseState = seed
        var filteredNoise = 0.0
        for (index in input.indices) {
            noiseState = noiseState * 1664525 + 1013904223
            val whiteNoise = (noiseState ushr 8) / 8388608.0 - 1.0
            filteredNoise += smoothing * (whiteNoise - filteredNoise)
            val modulation = 0.75 + 0.25 * sin(modulationStep * index)
            val fanNoise = filteredNoise * modulation * noiseLevel * amount
            output[index] = input[index] + fanNoise
        }
        return output
    }

    private fun applyAlien(
        input: DoubleArray,
        sampleRate: Int,
        amount: Double,
        carrierHz: Double,
        wobbleHz: Double,
        tremoloHz: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val output = DoubleArray(input.size)
        val carrierStep = 2.0 * PI * carrierHz / sampleRate
        val wobbleStep = 2.0 * PI * wobbleHz / sampleRate
        val tremoloStep = 2.0 * PI * tremoloHz / sampleRate
        for (index in input.indices) {
            val carrier = sin(carrierStep * index + 4.0 * sin(wobbleStep * index))
            val tremolo = 0.7 + 0.3 * sin(tremoloStep * index)
            val wet = input[index] * carrier * tremolo
            output[index] = input[index] * (1.0 - amount) + wet * amount
        }
        return output
    }

    private fun applyDrunk(
        input: DoubleArray,
        sampleRate: Int,
        amount: Double,
        maxDelayMs: Double,
        wobbleHz: Double,
        tremoloHz: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val maxDelay = (maxDelayMs / 1000.0 * sampleRate).roundToInt().coerceAtLeast(2)
        val delayLine = DoubleArray(maxDelay + 1)
        val output = DoubleArray(input.size)
        val wobbleStep = 2.0 * PI * wobbleHz / sampleRate
        val tremoloStep = 2.0 * PI * tremoloHz / sampleRate
        var writeIndex = 0
        for (index in input.indices) {
            delayLine[writeIndex] = input[index]
            val wobble = 0.5 + 0.5 * sin(wobbleStep * index)
            val delaySamples = 1.0 + wobble * maxDelay * amount
            var readPosition = writeIndex - delaySamples
            if (readPosition < 0.0) readPosition += delayLine.size
            val readIndex = floor(readPosition).toInt()
            val fraction = readPosition - readIndex
            val first = delayLine[readIndex % delayLine.size]
            val second = delayLine[(readIndex + 1) % delayLine.size]
            val delayed = first * (1.0 - fraction) + second * fraction
            val tremolo = 1.0 - 0.25 * amount * (0.5 + 0.5 * sin(tremoloStep * index))
            val wet = delayed * tremolo
            output[index] = input[index] * (1.0 - amount * 0.6) + wet * amount * 0.6
            writeIndex = (writeIndex + 1) % delayLine.size
        }
        return output
    }

    private fun applyStutter(
        input: DoubleArray,
        sampleRate: Int,
        amount: Double,
        sliceMs: Double,
        initialSeed: Int,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val blockSize = (sliceMs / 1000.0 * sampleRate).roundToInt().coerceAtLeast(1)
        val output = input.copyOf()
        var seed = initialSeed
        var start = 0
        while (start < input.size) {
            val blockEnd = minOf(start + blockSize, input.size)
            val blockLength = blockEnd - start
            seed = seed * 1664525 + 1013904223
            val trigger = ((seed ushr 8) and 0xFFFF) / 65535.0 < amount
            if (trigger && blockLength > 0) {
                seed = seed * 1664525 + 1013904223
                val extraRepeats = 1 + ((seed ushr 8) and 0x01)
                var destination = blockEnd
                var repeat = 0
                while (repeat < extraRepeats && destination < input.size) {
                    val copyLength = minOf(blockLength, input.size - destination)
                    input.copyInto(output, destination, start, start + copyLength)
                    destination += copyLength
                    repeat++
                }
                start = destination
            } else {
                start = blockEnd
            }
        }
        return output
    }

    private fun applyTelephone(
        input: DoubleArray,
        sampleRate: Int,
        amount: Double,
        lowHz: Double,
        highHz: Double,
        drive: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        var processed = applyHighPass(input, sampleRate, lowHz)
        processed = applyLowPass(processed, sampleRate, highHz)
        val saturated = DoubleArray(processed.size) { index ->
            tanh(processed[index] * drive)
        }
        return blend(input, saturated, amount)
    }

    private fun applyMegaphone(
        input: DoubleArray,
        sampleRate: Int,
        amount: Double,
        lowHz: Double,
        highHz: Double,
        drive: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        var processed = applyHighPass(input, sampleRate, lowHz)
        processed = applyLowPass(processed, sampleRate, highHz)
        val driven = DoubleArray(processed.size) { index ->
            tanh(processed[index] * drive)
        }
        return blend(input, driven, amount)
    }

    private fun applyBee(
        input: DoubleArray,
        sampleRate: Int,
        amount: Double,
        buzzHz: Double,
        buzzRatio: Double,
        flutterHz: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val output = DoubleArray(input.size)
        val buzzStep = 2.0 * PI * buzzHz / sampleRate
        val flutterStep = 2.0 * PI * flutterHz / sampleRate
        for (index in input.indices) {
            val flutter = sin(flutterStep * index)
            val buzz = 0.5 * sin(buzzStep * index + flutter) +
                0.5 * sin(buzzStep * buzzRatio * index - flutter)
            val wet = input[index] * (0.5 + 0.5 * buzz)
            output[index] = input[index] * (1.0 - amount) + wet * amount
        }
        return output
    }

    private fun applyLowPass(
        input: DoubleArray,
        sampleRate: Int,
        cutoffHz: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val dt = 1.0 / sampleRate
        val rc = 1.0 / (2.0 * PI * cutoffHz)
        val alpha = dt / (rc + dt)
        val output = DoubleArray(input.size)
        var previous = input[0]
        for (index in input.indices) {
            previous += alpha * (input[index] - previous)
            output[index] = previous
        }
        return output
    }

    private fun applyOutputGain(input: DoubleArray, gainDb: Double): DoubleArray {
        val gain = 10.0.pow(gainDb / 20.0)
        return DoubleArray(input.size) { index ->
            (input[index] * gain).coerceIn(-1.0, 1.0)
        }
    }

    private fun applyRobot(
        input: DoubleArray,
        amount: Double,
        sampleRate: Int,
        carrierHz: Double,
    ): DoubleArray {
        if (sampleRate <= 0) return input

        val phaseStep = 2.0 * PI * carrierHz / sampleRate
        return DoubleArray(input.size) { index ->
            val dry = input[index]
            val modulated = dry * sin(phaseStep * index)
            dry * (1.0 - amount) + modulated * amount
        }
    }

    private fun applyGrit(
        input: DoubleArray,
        amount: Double,
        maxDrive: Double,
    ): DoubleArray {
        var peak = 0.0
        for (sample in input) {
            val magnitude = abs(sample)
            if (magnitude > peak) peak = magnitude
        }
        if (peak < MIN_PEAK) return input

        val drive = 1.0 + amount * maxDrive
        val normalization = tanh(drive * peak) / peak
        return DoubleArray(input.size) { index ->
            val dry = input[index]
            val saturated = tanh(dry * drive) / normalization
            dry * (1.0 - amount) + saturated * amount
        }
    }

    private companion object {
        const val MIN_SPEED = 0.5
        const val MAX_SPEED = 1.5
        const val MIN_EFFECT = 0.0
        const val MAX_EFFECT = 1.0
        const val MIN_HIGH_PASS_HZ = 0.0
        const val MAX_HIGH_PASS_HZ = 300.0
        const val MIN_NOISE_GATE_DB = -80.0
        const val MAX_NOISE_GATE_DB = -20.0
        const val MIN_OUTPUT_GAIN_DB = -12.0
        const val MAX_OUTPUT_GAIN_DB = 12.0
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
        const val MIN_PEAK = 1e-9
        const val DEFAULT_SAMPLE_RATE = 44_100
    }
}
