package com.voicedistorcer.processing

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

data class VideoProcessingResult(
    val durationMs: Long,
)

object VideoAudioProcessor {
    fun process(
        inputFile: File,
        outputFile: File,
        config: DistortionConfig,
    ): VideoProcessingResult {
        require(inputFile.exists()) { "Input video does not exist" }

        val extractor = MediaExtractor()
        val videoFormat: MediaFormat
        val videoDurationUs: Long
        try {
            extractor.setDataSource(inputFile.absolutePath)
            val videoTrackIndex = findTrack(extractor, "video/")
            require(videoTrackIndex >= 0) { "Video track not found" }

            videoFormat = extractor.getTrackFormat(videoTrackIndex)
            videoDurationUs = videoFormat.getLongOrDefault(MediaFormat.KEY_DURATION, 0L)
        } finally {
            extractor.release()
        }

        val decodedAudio = MediaAudioDecoder.decode(inputFile)
        val syncedConfig = config.copy(speed = 1.0)
        val processed = VoiceDistortionProcessor(syncedConfig)
            .process(decodedAudio.samples, decodedAudio.sampleRate)
        require(processed.isNotEmpty()) { "Processed audio is empty" }

        val targetSamples = if (videoDurationUs > 0) {
            (videoDurationUs * decodedAudio.sampleRate / 1_000_000L).toInt()
        } else {
            processed.size
        }
        val syncedAudio = if (targetSamples > 0) {
            processed.copyOf(targetSamples)
        } else {
            processed
        }
        val encodedAudio = encodeAudio(syncedAudio, decodedAudio.sampleRate)

        muxVideoWithAudio(
            inputFile = inputFile,
            videoFormat = videoFormat,
            encodedAudio = encodedAudio,
            outputFile = outputFile,
        )

        return VideoProcessingResult(
            durationMs = syncedAudio.size * 1_000L / decodedAudio.sampleRate,
        )
    }

    private fun encodeAudio(pcm: ShortArray, sampleRate: Int): EncodedAudio {
        val format = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC,
            sampleRate,
            1,
        ).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, 128_000)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16_384)
        }

        val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        val samples = ArrayList<EncodedSample>()
        var outputFormat: MediaFormat? = null
        val info = MediaCodec.BufferInfo()
        var inputOffset = 0
        var inputDone = false
        var outputDone = false
        val totalDurationUs = pcm.size * 1_000_000L / sampleRate

        try {
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            while (!outputDone) {
                if (!inputDone) {
                    val inputIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inputIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputIndex)
                            ?: throw IllegalStateException("Encoder input buffer unavailable")
                        inputBuffer.clear()

                        val remaining = pcm.size - inputOffset
                        if (remaining <= 0) {
                            codec.queueInputBuffer(
                                inputIndex,
                                0,
                                0,
                                totalDurationUs,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM,
                            )
                            inputDone = true
                        } else {
                            val maxSamples = inputBuffer.capacity() / BYTES_PER_SAMPLE
                            require(maxSamples > 0) { "Encoder input buffer is too small" }
                            val sampleCount = min(remaining, maxSamples)
                            inputBuffer.order(ByteOrder.LITTLE_ENDIAN)
                            inputBuffer.asShortBuffer().put(pcm, inputOffset, sampleCount)

                            val presentationTimeUs = inputOffset * 1_000_000L / sampleRate
                            codec.queueInputBuffer(
                                inputIndex,
                                0,
                                sampleCount * BYTES_PER_SAMPLE,
                                presentationTimeUs,
                                0,
                            )
                            inputOffset += sampleCount
                        }
                    }
                }

                when (val outputIndex = codec.dequeueOutputBuffer(info, TIMEOUT_US)) {
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        outputFormat = codec.outputFormat
                    }
                    else -> {
                        if (outputIndex >= 0) {
                            val outputBuffer = codec.getOutputBuffer(outputIndex)
                                ?: throw IllegalStateException("Encoder output buffer unavailable")

                            if ((info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                info.size = 0
                            }
                            if (info.size > 0) {
                                outputBuffer.position(info.offset)
                                outputBuffer.limit(info.offset + info.size)
                                val data = ByteArray(info.size)
                                outputBuffer.get(data)
                                samples.add(
                                    EncodedSample(
                                        data = data,
                                        presentationTimeUs = info.presentationTimeUs,
                                    ),
                                )
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
        }

        val formatOut = outputFormat ?: throw IllegalStateException("Audio encoder format missing")
        require(samples.isNotEmpty()) { "Encoded audio is empty" }
        return EncodedAudio(format = formatOut, samples = samples)
    }

    private fun muxVideoWithAudio(
        inputFile: File,
        videoFormat: MediaFormat,
        encodedAudio: EncodedAudio,
        outputFile: File,
    ) {
        outputFile.parentFile?.mkdirs()
        if (outputFile.exists()) outputFile.delete()

        val extractor = MediaExtractor()
        val muxer = MediaMuxer(
            outputFile.absolutePath,
            MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4,
        )

        try {
            extractor.setDataSource(inputFile.absolutePath)
            val videoTrackIndex = findTrack(extractor, "video/")
            require(videoTrackIndex >= 0) { "Video track not found" }
            extractor.selectTrack(videoTrackIndex)

            val rotation = resolveRotation(videoFormat, inputFile)
            if (rotation != 0) {
                muxer.setOrientationHint(rotation)
            }
            val outputVideoTrack = muxer.addTrack(videoFormat)
            val outputAudioTrack = muxer.addTrack(encodedAudio.format)
            muxer.start()

            val maxInputSize = if (videoFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } else {
                DEFAULT_VIDEO_BUFFER_SIZE
            }
            val videoBuffer = ByteBuffer.allocateDirect(
                maxInputSize.coerceAtLeast(DEFAULT_VIDEO_BUFFER_SIZE),
            )
            val videoInfo = MediaCodec.BufferInfo()
            var hasVideo = readNextVideoSample(extractor, videoBuffer, videoInfo)
            var audioIndex = 0

            while (hasVideo || audioIndex < encodedAudio.samples.size) {
                val videoTime = if (hasVideo) videoInfo.presentationTimeUs else Long.MAX_VALUE
                val audioSample = encodedAudio.samples.getOrNull(audioIndex)
                val audioTime = audioSample?.presentationTimeUs ?: Long.MAX_VALUE

                if (videoTime <= audioTime) {
                    muxer.writeSampleData(outputVideoTrack, videoBuffer, videoInfo)
                    hasVideo = readNextVideoSample(extractor, videoBuffer, videoInfo)
                } else if (audioSample != null) {
                    val audioInfo = MediaCodec.BufferInfo()
                    audioInfo.set(
                        0,
                        audioSample.data.size,
                        audioSample.presentationTimeUs,
                        0,
                    )
                    muxer.writeSampleData(
                        outputAudioTrack,
                        ByteBuffer.wrap(audioSample.data),
                        audioInfo,
                    )
                    audioIndex++
                }
            }
        } finally {
            runCatching { muxer.stop() }
            runCatching { muxer.release() }
            runCatching { extractor.release() }
        }
    }

    private fun readNextVideoSample(
        extractor: MediaExtractor,
        buffer: ByteBuffer,
        info: MediaCodec.BufferInfo,
    ): Boolean {
        buffer.clear()
        val size = extractor.readSampleData(buffer, 0)
        if (size < 0) return false

        val bufferFlags = if (
            (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0
        ) {
            MediaCodec.BUFFER_FLAG_KEY_FRAME
        } else {
            0
        }
        info.set(0, size, extractor.sampleTime, bufferFlags)
        buffer.position(0)
        buffer.limit(size)
        extractor.advance()
        return true
    }

    private fun resolveRotation(format: MediaFormat, file: File): Int {
        val fromFormat = if (format.containsKey(MediaFormat.KEY_ROTATION)) {
            format.getInteger(MediaFormat.KEY_ROTATION)
        } else {
            0
        }
        return if (fromFormat != 0) fromFormat else readRotation(file)
    }

    private fun readRotation(file: File): Int {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                ?.toIntOrNull()
                ?: 0
        } catch (_: Exception) {
            0
        } finally {
            runCatching { retriever.release() }
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

    private fun MediaFormat.getLongOrDefault(key: String, default: Long): Long =
        if (containsKey(key)) getLong(key) else default

    private data class EncodedSample(
        val data: ByteArray,
        val presentationTimeUs: Long,
    )

    private data class EncodedAudio(
        val format: MediaFormat,
        val samples: List<EncodedSample>,
    )

    private const val TIMEOUT_US = 10_000L
    private const val BYTES_PER_SAMPLE = 2
    private const val DEFAULT_VIDEO_BUFFER_SIZE = 4 * 1024 * 1024
}
