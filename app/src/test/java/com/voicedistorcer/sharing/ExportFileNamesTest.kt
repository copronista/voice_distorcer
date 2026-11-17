package com.voicedistorcer.sharing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportFileNamesTest {
    @Test
    fun audioNameHasBase36Suffix() {
        assertTrue(AUDIO_PATTERN.matches(ExportFileNames.audio()))
    }

    @Test
    fun audioSuffixesAreUnique() {
        val names = List(20) { ExportFileNames.audio() }
        assertEquals(names.size, names.toSet().size)
    }

    private companion object {
        val AUDIO_PATTERN = Regex("^anonymized_audio_[0-9a-z]{12}\\.m4a$")
    }
}
