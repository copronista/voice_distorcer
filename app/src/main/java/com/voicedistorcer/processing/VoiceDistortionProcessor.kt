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
            processed = applyNoiseGate(processed, sampleRate, gateDb)
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
            processed = applyRobot(processed, robot, sampleRate)
        }

        val alien = config.alien.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (alien > 0.0 && processed.isNotEmpty()) {
            processed = applyAlien(processed, sampleRate, alien)
        }

        val drunk = config.drunk.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (drunk > 0.0 && processed.isNotEmpty()) {
            processed = applyDrunk(processed, sampleRate, drunk)
        }

        val grit = config.grit.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (grit > 0.0 && processed.isNotEmpty()) {
            processed = applyGrit(processed, grit)
        }

        val compressor = config.compressor.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (compressor > 0.0 && processed.isNotEmpty()) {
            processed = applyCompressor(processed, sampleRate, compressor)
        }

        if (config.reverse && processed.isNotEmpty()) {
            processed = processed.reversedArray()
        }

        val stutter = config.stutter.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (stutter > 0.0 && processed.isNotEmpty()) {
            processed = applyStutter(processed, sampleRate, stutter)
        }

        val telephone = config.telephone.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (telephone > 0.0 && processed.isNotEmpty()) {
            processed = applyTelephone(processed, sampleRate, telephone)
        }

        val megaphone = config.megaphone.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (megaphone > 0.0 && processed.isNotEmpty()) {
            processed = applyMegaphone(processed, sampleRate, megaphone)
        }

        val bee = config.bee.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (bee > 0.0 && processed.isNotEmpty()) {
            processed = applyBee(processed, sampleRate, bee)
        }

        val echo = config.echo.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (echo > 0.0 && processed.isNotEmpty()) {
            processed = applyEcho(processed, sampleRate, echo)
        }

        val fan = config.fan.coerceIn(MIN_EFFECT, MAX_EFFECT)
        if (fan > 0.0 && processed.isNotEmpty()) {
            processed = applyFan(processed, sampleRate, fan)
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
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val threshold = 10.0.pow(thresholdDb / 20.0)
        val attack = exp(-1.0 / (GATE_ATTACK_SECONDS * sampleRate))
        val release = exp(-1.0 / (GATE_RELEASE_SECONDS * sampleRate))
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
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val ratio = 1.0 + amount * MAX_COMPRESSOR_RATIO
        val attack = exp(-1.0 / (COMPRESSOR_ATTACK_SECONDS * sampleRate))
        val release = exp(-1.0 / (COMPRESSOR_RELEASE_SECONDS * sampleRate))
        val output = DoubleArray(input.size)
        var envelope = 0.0
        for (index in input.indices) {
            val sample = input[index]
            val level = abs(sample)
            val coefficient = if (level > envelope) attack else release
            envelope = coefficient * envelope + (1.0 - coefficient) * level
            val gain = if (envelope > COMPRESSOR_THRESHOLD) {
                (COMPRESSOR_THRESHOLD + (envelope - COMPRESSOR_THRESHOLD) / ratio) / envelope
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
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val delaySamples = (ECHO_DELAY_SECONDS * sampleRate).roundToInt().coerceAtLeast(1)
        val delay = DoubleArray(delaySamples)
        val wet = amount * ECHO_MAX_WET
        val feedback = amount * ECHO_MAX_FEEDBACK
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
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val output = DoubleArray(input.size)
        val smoothing = 1.0 - exp(-2.0 * PI * FAN_NOISE_CUTOFF_HZ / sampleRate)
        val modulationStep = 2.0 * PI * FAN_MODULATION_HZ / sampleRate
        var noiseState = FAN_NOISE_SEED
        var filteredNoise = 0.0
        for (index in input.indices) {
            noiseState = noiseState * 1664525 + 1013904223
            val whiteNoise = (noiseState ushr 8) / 8388608.0 - 1.0
            filteredNoise += smoothing * (whiteNoise - filteredNoise)
            val modulation = 0.75 + 0.25 * sin(modulationStep * index)
            val fanNoise = filteredNoise * modulation * FAN_NOISE_LEVEL * amount
            output[index] = input[index] + fanNoise
        }
        return output
    }

    private fun applyAlien(
        input: DoubleArray,
        sampleRate: Int,
        amount: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val output = DoubleArray(input.size)
        val carrierStep = 2.0 * PI * ALIEN_CARRIER_HZ / sampleRate
        val wobbleStep = 2.0 * PI * ALIEN_WOBBLE_HZ / sampleRate
        val tremoloStep = 2.0 * PI * ALIEN_TREMOLO_HZ / sampleRate
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
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val maxDelay = (DRUNK_MAX_DELAY_SECONDS * sampleRate).roundToInt().coerceAtLeast(2)
        val delayLine = DoubleArray(maxDelay + 1)
        val output = DoubleArray(input.size)
        val wobbleStep = 2.0 * PI * DRUNK_WOBBLE_HZ / sampleRate
        val tremoloStep = 2.0 * PI * DRUNK_TREMOLO_HZ / sampleRate
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
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val blockSize = (STUTTER_SLICE_SECONDS * sampleRate).roundToInt().coerceAtLeast(1)
        val output = input.copyOf()
        var seed = STUTTER_SEED
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
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        var processed = applyHighPass(input, sampleRate, TELEPHONE_LOW_HZ)
        processed = applyLowPass(processed, sampleRate, TELEPHONE_HIGH_HZ)
        val saturated = DoubleArray(processed.size) { index ->
            tanh(processed[index] * TELEPHONE_DRIVE)
        }
        return blend(input, saturated, amount)
    }

    private fun applyMegaphone(
        input: DoubleArray,
        sampleRate: Int,
        amount: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        var processed = applyHighPass(input, sampleRate, MEGAPHONE_LOW_HZ)
        processed = applyLowPass(processed, sampleRate, MEGAPHONE_HIGH_HZ)
        val driven = DoubleArray(processed.size) { index ->
            tanh(processed[index] * MEGAPHONE_DRIVE)
        }
        return blend(input, driven, amount)
    }

    private fun applyBee(
        input: DoubleArray,
        sampleRate: Int,
        amount: Double,
    ): DoubleArray {
        if (input.isEmpty() || sampleRate <= 0) return input

        val output = DoubleArray(input.size)
        val buzzStep = 2.0 * PI * BEE_BUZZ_HZ / sampleRate
        val flutterStep = 2.0 * PI * BEE_FLUTTER_HZ / sampleRate
        for (index in input.indices) {
            val flutter = sin(flutterStep * index)
            val buzz = 0.5 * sin(buzzStep * index + flutter) +
                0.5 * sin(buzzStep * BEE_BUZZ_RATIO * index - flutter)
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

    private fun applyRobot(input: DoubleArray, amount: Double, sampleRate: Int): DoubleArray {
        if (sampleRate <= 0) return input

        val phaseStep = 2.0 * PI * ROBOT_CARRIER_HZ / sampleRate
        return DoubleArray(input.size) { index ->
            val dry = input[index]
            val modulated = dry * sin(phaseStep * index)
            dry * (1.0 - amount) + modulated * amount
        }
    }

    private fun applyGrit(input: DoubleArray, amount: Double): DoubleArray {
        var peak = 0.0
        for (sample in input) {
            val magnitude = abs(sample)
            if (magnitude > peak) peak = magnitude
        }
        if (peak < MIN_PEAK) return input

        val drive = 1.0 + amount * MAX_GRIT_DRIVE
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
        const val MAX_GRIT_DRIVE = 4.0
        const val ECHO_DELAY_SECONDS = 0.18
        const val ECHO_MAX_WET = 0.7
        const val ECHO_MAX_FEEDBACK = 0.45
        const val FAN_NOISE_CUTOFF_HZ = 1200.0
        const val FAN_MODULATION_HZ = 0.3
        const val FAN_NOISE_LEVEL = 0.18
        const val FAN_NOISE_SEED = 0x12345678
        const val STUTTER_SLICE_SECONDS = 0.12
        const val STUTTER_SEED = 0x6D2B79F5
        const val ALIEN_CARRIER_HZ = 220.0
        const val ALIEN_WOBBLE_HZ = 0.6
        const val ALIEN_TREMOLO_HZ = 5.0
        const val DRUNK_MAX_DELAY_SECONDS = 0.015
        const val DRUNK_WOBBLE_HZ = 0.8
        const val DRUNK_TREMOLO_HZ = 4.5
        const val TELEPHONE_LOW_HZ = 300.0
        const val TELEPHONE_HIGH_HZ = 3400.0
        const val TELEPHONE_DRIVE = 2.0
        const val MEGAPHONE_LOW_HZ = 500.0
        const val MEGAPHONE_HIGH_HZ = 3000.0
        const val MEGAPHONE_DRIVE = 4.0
        const val BEE_BUZZ_HZ = 35.0
        const val BEE_BUZZ_RATIO = 1.17
        const val BEE_FLUTTER_HZ = 0.7
        const val MAX_COMPRESSOR_RATIO = 7.0
        const val COMPRESSOR_THRESHOLD = 0.25
        const val COMPRESSOR_ATTACK_SECONDS = 0.005
        const val COMPRESSOR_RELEASE_SECONDS = 0.100
        const val GATE_ATTACK_SECONDS = 0.005
        const val GATE_RELEASE_SECONDS = 0.100
        const val MIN_PEAK = 1e-9
        const val ROBOT_CARRIER_HZ = 80.0
        const val DEFAULT_SAMPLE_RATE = 44_100
    }
}
