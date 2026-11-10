package com.voicedistorcer.processing

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.roundToInt

data class DecodedAudio(
    val samples: ShortArray,
    val sampleRate: Int,
)

object MediaAudioDecoder {
    fun decode(file: File): DecodedAudio {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(file.absolutePath)
            val audioTrackIndex = findTrack(extractor, "audio/")
            require(audioTrackIndex >= 0) { "File has no audio track" }
            return decodeTrack(
                extractor = extractor,
                trackIndex = audioTrackIndex,
                format = extractor.getTrackFormat(audioTrackIndex),
            )
        } finally {
            extractor.release()
        }
    }

    private fun decodeTrack(
        extractor: MediaExtractor,
        trackIndex: Int,
        format: MediaFormat,
    ): DecodedAudio {
        val mime = format.getString(MediaFormat.KEY_MIME)
            ?: throw IllegalStateException("Audio track has no MIME type")
        var sampleRate = format.getIntOrDefault(MediaFormat.KEY_SAMPLE_RATE, 0)
        var channels = format.getIntOrDefault(MediaFormat.KEY_CHANNEL_COUNT, 1)
        var pcmEncoding = format.getIntOrDefault(
            MediaFormat.KEY_PCM_ENCODING,
            AudioFormat.ENCODING_PCM_16BIT,
        )

        extractor.selectTrack(trackIndex)
        val codec = MediaCodec.createDecoderByType(mime)
        val chunks = ArrayList<ShortArray>()
        var totalSamples = 0
        val info = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false

        try {
            codec.configure(format, null, null, 0)
            codec.start()

            while (!outputDone) {
                if (!inputDone) {
                    val inputIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inputIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputIndex)
                            ?: throw IllegalStateException("Decoder input buffer unavailable")
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(
                                inputIndex,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM,
                            )
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(
                                inputIndex,
                                0,
                                sampleSize,
                                extractor.sampleTime,
                                0,
                            )
                            extractor.advance()
                        }
                    }
                }

                when (val outputIndex = codec.dequeueOutputBuffer(info, TIMEOUT_US)) {
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val outputFormat = codec.outputFormat
                        sampleRate = outputFormat.getIntOrDefault(
                            MediaFormat.KEY_SAMPLE_RATE,
                            sampleRate,
                        )
                        channels = outputFormat.getIntOrDefault(
                            MediaFormat.KEY_CHANNEL_COUNT,
                            channels,
                        )
                        pcmEncoding = outputFormat.getIntOrDefault(
                            MediaFormat.KEY_PCM_ENCODING,
                            AudioFormat.ENCODING_PCM_16BIT,
                        )
                    }
                    else -> {
                        if (outputIndex >= 0) {
                            val outputBuffer = codec.getOutputBuffer(outputIndex)
                                ?: throw IllegalStateException("Decoder output buffer unavailable")
                            if (info.size > 0) {
                                val mono = toMono(
                                    buffer = outputBuffer,
                                    offset = info.offset,
                                    size = info.size,
                                    channels = channels,
                                    encoding = pcmEncoding,
                                )
                                if (mono.isNotEmpty()) {
                                    chunks.add(mono)
                                    totalSamples += mono.size
                                }
                            }
                            codec.releaseOutputBuffer(outputIndex, false)
                            if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                outputDone = true
                            }
                        }
                    }
                }
            }
        } finally {
            runCatching { codec.stop() }
            codec.release()
            runCatching { extractor.unselectTrack(trackIndex) }
        }

        require(sampleRate > 0) { "Audio sample rate is missing" }
        require(totalSamples > 0) { "File has no decodable audio" }

        val samples = ShortArray(totalSamples)
        var offset = 0
        for (chunk in chunks) {
            chunk.copyInto(samples, offset)
            offset += chunk.size
        }
        return DecodedAudio(samples = samples, sampleRate = sampleRate)
    }

    private fun toMono(
        buffer: ByteBuffer,
        offset: Int,
        size: Int,
        channels: Int,
        encoding: Int,
    ): ShortArray {
        require(channels > 0) { "Invalid audio channel count" }
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(offset)
        buffer.limit(offset + size)

        return when (encoding) {
            AudioFormat.ENCODING_PCM_16BIT -> {
                val shorts = buffer.asShortBuffer()
                val frameCount = shorts.remaining() / channels
                ShortArray(frameCount) { frame ->
                    var sum = 0
                    for (channel in 0 until channels) {
                        sum += shorts.get(frame * channels + channel)
                    }
                    (sum / channels).coerceIn(-32768, 32767).toShort()
                }
            }
            AudioFormat.ENCODING_PCM_FLOAT -> {
                val floats = buffer.asFloatBuffer()
                val frameCount = floats.remaining() / channels
                ShortArray(frameCount) { frame ->
                    var sum = 0.0
                    for (channel in 0 until channels) {
                        sum += floats.get(frame * channels + channel)
                    }
                    ((sum / channels) * 32767.0).roundToInt().coerceIn(-32768, 32767).toShort()
                }
            }
            AudioFormat.ENCODING_PCM_8BIT -> {
                val frameCount = size / channels
                ShortArray(frameCount) { frame ->
                    var sum = 0
                    for (channel in 0 until channels) {
                        val value =
                            (buffer.get(offset + frame * channels + channel).toInt() and 0xFF) - 128
                        sum += value * 256
                    }
                    (sum / channels).coerceIn(-32768, 32767).toShort()
                }
            }
            else -> throw IllegalStateException("Unsupported PCM encoding: $encoding")
        }
    }

    private fun findTrack(extractor: MediaExtractor, mimePrefix: String): Int {
        for (index in 0 until extractor.trackCount) {
            val mime = extractor.getTrackFormat(index).getString(MediaFormat.KEY_MIME)
                ?: continue
            if (mime.startsWith(mimePrefix)) return index
        }
        return -1
    }

    private fun MediaFormat.getIntOrDefault(key: String, default: Int): Int =
        if (containsKey(key)) getInteger(key) else default

    private const val TIMEOUT_US = 10_000L
}
