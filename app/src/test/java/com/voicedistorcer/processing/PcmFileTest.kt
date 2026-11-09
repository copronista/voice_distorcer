package com.voicedistorcer.processing

import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.io.File

class PcmFileTest {
    @Test
    fun readsLittleEndianSamples() {
        val samples = shortArrayOf(Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE)
        val file = File.createTempFile("voice-distorcer", ".pcm")
        try {
            file.outputStream().use { output ->
                for (sample in samples) {
                    output.write(sample.toInt() and 0xFF)
                    output.write((sample.toInt() shr 8) and 0xFF)
                }
            }

            assertArrayEquals(samples, PcmFile.read(file))
        } finally {
            file.delete()
        }
    }

    @Test
    fun ignoresTrailingOddByte() {
        val file = File.createTempFile("voice-distorcer", ".pcm")
        try {
            file.writeBytes(byteArrayOf(0x34, 0x12, 0x7F))

            assertArrayEquals(shortArrayOf(0x1234), PcmFile.read(file))
        } finally {
            file.delete()
        }
    }
}
