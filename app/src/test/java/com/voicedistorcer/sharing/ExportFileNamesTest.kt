package com.voicedistorcer.sharing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ExportFileNamesTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun audioNameHasBase36Suffix() {
        assertTrue(AUDIO_PATTERN.matches(ExportFileNames.audio()))
    }

    @Test
    fun suffixesAreUnique() {
        val audioNames = List(20) { ExportFileNames.audio() }
        assertEquals(audioNames.size, audioNames.toSet().size)
    }

    @Test
    fun customBaseNameGetsSuffixWhenRequested() {
        assertTrue(Regex("^mio_audio_[0-9a-z]{12}\\.m4a$").matches(ExportFileNames.audio("mio_audio")))
    }

    @Test
    fun customBaseNameIsExactWithoutSuffix() {
        assertEquals("mio_audio.m4a", ExportFileNames.audio("mio_audio", false))
    }

    @Test
    fun blankBaseNameFallsBackToBuiltInDefault() {
        assertEquals("anonymized_audio.m4a", ExportFileNames.audio("  ", false))
        assertEquals("anonymized_audio.m4a", ExportFileNames.audio(null, false))
    }

    @Test
    fun invalidCharactersAndExtensionAreRemoved() {
        assertEquals(
            "mioaudio name.m4a",
            ExportFileNames.audio("mio/audio: name?.m4a", false),
        )
    }

    @Test
    fun resolveDeletesAnExistingTargetWhenSuffixIsDisabled() {
        val directory = temporaryFolder.newFolder()
        val existing = File(directory, "same.m4a")
        existing.writeText("old")

        val resolved = ExportFileNames.resolveAudio(directory, "same", false)

        assertEquals("same.m4a", resolved.name)
        assertTrue(!resolved.exists())
    }

    @Test
    fun renameKeepsTheExtensionAndContent() {
        val directory = temporaryFolder.newFolder()
        val original = File(directory, "anonymized_audio_abcdefghijkl.m4a")
        original.writeText("voice data")

        val renamed = ExportFileNames.rename(original, "mio_audio", false)

        assertEquals("mio_audio.m4a", renamed?.name)
        assertEquals("voice data", renamed?.readText())
    }

    private companion object {
        val AUDIO_PATTERN = Regex("^anonymized_audio_[0-9a-z]{12}\\.m4a$")
    }
}
