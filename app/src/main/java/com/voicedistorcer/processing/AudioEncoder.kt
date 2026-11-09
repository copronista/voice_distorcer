package com.voicedistorcer.processing

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.nio.ByteOrder
import kotlin.math.min

object AudioEncoder {
    private const val BYTES_PER_SAMPLE = 2
    private const val TIMEOUT_US = 10_000L

    fun encodeToM4a(pcm: ShortArray, sampleRate: Int, outputFile: File) {
        require(pcm.isNotEmpty()) { "Cannot encode an empty clip" }

        outputFile.parentFile?.mkdirs()
        if (outputFile.exists()) outputFile.delete()

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
        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false

        try {
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            val bufferInfo = MediaCodec.BufferInfo()
            var inputOffset = 0
            var inputDone = false
            var outputDone = false
            val totalDurationUs = pcm.size * 1_000_000L / sampleRate

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

                when (val outputIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)) {
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        check(!muxerStarted) { "Encoder output format changed twice" }
                        trackIndex = muxer.addTrack(codec.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                    else -> {
                        if (outputIndex >= 0) {
                            val outputBuffer = codec.getOutputBuffer(outputIndex)
                                ?: throw IllegalStateException("Encoder output buffer unavailable")

                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                bufferInfo.size = 0
                            }
                            if (bufferInfo.size > 0) {
                                check(muxerStarted) { "Muxer not started before writing samples" }
                                outputBuffer.position(bufferInfo.offset)
                                outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                                muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                            }

                            codec.releaseOutputBuffer(outputIndex, false)
                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                outputDone = true
                            }
                        }
                    }
                }
            }
        } finally {
            runCatching { codec.stop() }
            codec.release()
            if (muxerStarted) runCatching { muxer.stop() }
            runCatching { muxer.release() }
        }
    }
}
