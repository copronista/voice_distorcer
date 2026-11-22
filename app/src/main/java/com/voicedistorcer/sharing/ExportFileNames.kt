package com.voicedistorcer.sharing

import com.voicedistorcer.settings.VoiceSettings
import java.io.File
import java.security.SecureRandom
import java.util.Locale

object ExportFileNames {
    fun audio(baseName: String? = null, uniqueSuffix: Boolean = true): String =
        fileName(baseName, uniqueSuffix, AUDIO_EXTENSION)

    fun resolveAudio(
        directory: File,
        baseName: String? = null,
        uniqueSuffix: Boolean = true,
    ): File = resolve(directory, baseName, uniqueSuffix, AUDIO_EXTENSION)

    fun rename(current: File, baseName: String?, uniqueSuffix: Boolean): File? {
        val extension = current.name.substringAfterLast('.', "").lowercase(Locale.US)
            .ifEmpty { AUDIO_EXTENSION }
        val parent = current.parentFile ?: return null
        val target = resolve(parent, baseName, uniqueSuffix, extension)
        if (target.absolutePath == current.absolutePath) return current
        return if (current.renameTo(target)) target else null
    }

    private fun fileName(
        baseName: String?,
        uniqueSuffix: Boolean,
        extension: String,
    ): String {
        val base = sanitize(baseName)
        return if (uniqueSuffix) {
            "${base}_${randomSuffix()}.$extension"
        } else {
            "$base.$extension"
        }
    }

    private fun resolve(
        directory: File,
        baseName: String?,
        uniqueSuffix: Boolean,
        extension: String,
    ): File {
        var candidate = File(directory, fileName(baseName, uniqueSuffix, extension))
        while (uniqueSuffix && candidate.exists()) {
            candidate = File(directory, fileName(baseName, true, extension))
        }
        if (candidate.exists()) candidate.delete()
        return candidate
    }

    private fun sanitize(baseName: String?): String {
        val withoutExtension = KNOWN_EXTENSION.replace(baseName?.trim().orEmpty(), "")
        val cleaned = withoutExtension
            .filter { it !in INVALID_CHARACTERS && !it.isISOControl() }
            .trim()
            .trim('.')
            .trim()
            .take(MAX_BASE_LENGTH)
        return cleaned.ifEmpty { VoiceSettings.DEFAULT_AUDIO_NAME }
    }

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
    private const val AUDIO_EXTENSION = "m4a"
    private const val MAX_BASE_LENGTH = 80
    private val KNOWN_EXTENSION = Regex("\\.m4a$", RegexOption.IGNORE_CASE)
    private val INVALID_CHARACTERS = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
}
