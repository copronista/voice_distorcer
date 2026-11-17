package com.voicedistorcer.sharing

import java.security.SecureRandom

object ExportFileNames {
    fun audio(): String = "anonymized_audio_${randomSuffix()}.m4a"

    fun video(): String = "anonymized_video_${randomSuffix()}.mp4"

    private fun randomSuffix(): String {
        var value = random.nextLong() and RANDOM_MASK
        val suffix = CharArray(SUFFIX_LENGTH)
        for (index in suffix.indices.reversed()) {
            suffix[index] = ALPHABET[(value % BASE).toInt()]
            value /= BASE
        }
        return String(suffix)
    }

    private val random = SecureRandom()
    private const val SUFFIX_LENGTH = 12
    private const val BASE = 36L
    private const val RANDOM_MASK = 0x3FFF_FFFF_FFFF_FFFFL
    private const val ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz"
}
