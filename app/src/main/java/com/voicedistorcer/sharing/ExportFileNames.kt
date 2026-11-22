package com.voicedistorcer.sharing

import com.voicedistorcer.settings.VoiceSettings
import java.io.File
import java.security.SecureRandom
import java.util.Locale

object ExportFileNames {
    fun audio(baseName: String? = null, uniqueSuffix: Boolean = true): String =
        fileName(baseName, VoiceSettings.DEFAULT_AUDIO_NAME, uniqueSuffix, AUDIO_EXTENSION)

    fun video(baseName: String? = null, uniqueSuffix: Boolean = true): String =
        fileName(baseName, VoiceSettings.DEFAULT_VIDEO_NAME, uniqueSuffix, VIDEO_EXTENSION)

    fun resolveAudio(
        directory: File,
        baseName: String? = null,
        uniqueSuffix: Boolean = true,
    ): File = resolve(directory, baseName, VoiceSettings.DEFAULT_AUDIO_NAME, uniqueSuffix, AUDIO_EXTENSION)

    fun resolveVideo(
        directory: File,
        baseName: String? = null,
        uniqueSuffix: Boolean = true,
    ): File = resolve(directory, baseName, VoiceSettings.DEFAULT_VIDEO_NAME, uniqueSuffix, VIDEO_EXTENSION)

    fun rename(current: File, baseName: String?, uniqueSuffix: Boolean): File? {
        val extension = current.name.substringAfterLast('.', "").lowercase(Locale.US)
        val fallback = if (extension == VIDEO_EXTENSION) {
            VoiceSettings.DEFAULT_VIDEO_NAME
        } else {
            VoiceSettings.DEFAULT_AUDIO_NAME
        }
        val parent = current.parentFile ?: return null
        val target = resolve(parent, baseName, fallback, uniqueSuffix, extension)
        if (target.absolutePath == current.absolutePath) return current
        return if (current.renameTo(target)) target else null
    }

    private fun fileName(
        baseName: String?,
        fallback: String,
        uniqueSuffix: Boolean,
        extension: String,
    ): String {
        val base = sanitize(baseName, fallback)
        return if (uniqueSuffix) {
            "${base}_${randomSuffix()}.$extension"
        } else {
            "$base.$extension"
        }
    }

    private fun resolve(
        directory: File,
        baseName: String?,
        fallback: String,
        uniqueSuffix: Boolean,
        extension: String,
    ): File {
        var candidate = File(directory, fileName(baseName, fallback, uniqueSuffix, extension))
        while (uniqueSuffix && candidate.exists()) {
            candidate = File(directory, fileName(baseName, fallback, true, extension))
        }
        if (candidate.exists()) candidate.delete()
        return candidate
    }

    private fun sanitize(baseName: String?, fallback: String): String {
        val withoutExtension = KNOWN_EXTENSION.replace(baseName?.trim().orEmpty(), "")
        val cleaned = withoutExtension
            .filter { it !in INVALID_CHARACTERS && !it.isISOControl() }
            .trim()
            .trim('.')
            .trim()
            .take(MAX_BASE_LENGTH)
        return cleaned.ifEmpty { fallback }
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
    private const val VIDEO_EXTENSION = "mp4"
    private const val MAX_BASE_LENGTH = 80
    private val KNOWN_EXTENSION = Regex("\\.(m4a|mp4)$", RegexOption.IGNORE_CASE)
    private val INVALID_CHARACTERS = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
}
