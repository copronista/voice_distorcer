package com.voicedistorcer.recording

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

class RecordingEngine {
    @Volatile
    private var recording = false

    @Volatile
    private var audioRecord: AudioRecord? = null

    @SuppressLint("MissingPermission")
    suspend fun recordTo(outputFile: File, onStarted: () -> Unit = {}) = withContext(Dispatchers.IO) {
        val minimumBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING)
        if (minimumBufferSize <= 0) {
            throw IllegalStateException("Microphone is not available")
        }

        val bufferSamples = max(minimumBufferSize / BYTES_PER_SAMPLE, 2048)
        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL,
            ENCODING,
            bufferSamples * BYTES_PER_SAMPLE,
        )

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            throw IllegalStateException("Could not initialize the microphone")
        }

        audioRecord = record
        recording = true
        outputFile.parentFile?.mkdirs()

        try {
            record.startRecording()
            if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                throw IllegalStateException("Could not start recording")
            }
            onStarted()

            outputFile.outputStream().buffered().use { output ->
                val samples = ShortArray(bufferSamples)
                val bytes = ByteArray(bufferSamples * BYTES_PER_SAMPLE)

                while (recording) {
                    val read = record.read(samples, 0, samples.size)
                    when {
                        read > 0 -> {
                            var byteIndex = 0
                            for (index in 0 until read) {
                                val sample = samples[index].toInt()
                                bytes[byteIndex++] = (sample and 0xFF).toByte()
                                bytes[byteIndex++] = ((sample shr 8) and 0xFF).toByte()
                            }
                            output.write(bytes, 0, read * BYTES_PER_SAMPLE)
                        }
                        read == AudioRecord.ERROR_DEAD_OBJECT -> {
                            throw IllegalStateException("Microphone disconnected")
                        }
                        read < 0 -> {
                            throw IllegalStateException("Microphone read failed ($read)")
                        }
                    }
                }
            }
        } finally {
            recording = false
            runCatching { record.stop() }
            record.release()
            audioRecord = null
        }
    }

    fun stop() {
        recording = false
    }

    companion object {
        const val SAMPLE_RATE = 44_100
        private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private const val BYTES_PER_SAMPLE = 2
    }
}
