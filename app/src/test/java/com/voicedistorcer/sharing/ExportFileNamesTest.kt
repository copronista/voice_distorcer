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
    fun videoNameHasBase36Suffix() {
        assertTrue(VIDEO_PATTERN.matches(ExportFileNames.video()))
    }

    @Test
    fun suffixesAreUnique() {
        val audioNames = List(20) { ExportFileNames.audio() }
        val videoNames = List(20) { ExportFileNames.video() }
        assertEquals(audioNames.size, audioNames.toSet().size)
        assertEquals(videoNames.size, videoNames.toSet().size)
    }

    private companion object {
        val AUDIO_PATTERN = Regex("^anonymized_audio_[0-9a-z]{12}\\.m4a$")
        val VIDEO_PATTERN = Regex("^anonymized_video_[0-9a-z]{12}\\.mp4$")
    }
}
