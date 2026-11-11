package com.voicedistorcer.processing

import java.io.File

data class AudioProcessingResult(
    val durationMs: Long,
)

object AudioFileProcessor {
    fun process(
        inputFile: File,
        outputFile: File,
        config: DistortionConfig,
    ): AudioProcessingResult {
        require(inputFile.exists()) { "Input audio does not exist" }

        val decoded = MediaAudioDecoder.decode(inputFile)
        val processed = VoiceDistortionProcessor(config)
            .process(decoded.samples, decoded.sampleRate)
        require(processed.isNotEmpty()) { "Processed audio is empty" }

        AudioEncoder.encodeToM4a(processed, decoded.sampleRate, outputFile)
        return AudioProcessingResult(
            durationMs = processed.size * 1_000L / decoded.sampleRate,
        )
    }
}
