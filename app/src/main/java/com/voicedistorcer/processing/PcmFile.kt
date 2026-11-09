package com.voicedistorcer.processing

import java.io.File

object PcmFile {
    fun read(file: File): ShortArray {
        val bytes = file.readBytes()
        val sampleCount = bytes.size / 2
        val samples = ShortArray(sampleCount)

        for (index in 0 until sampleCount) {
            val low = bytes[index * 2].toInt() and 0xFF
            val high = bytes[index * 2 + 1].toInt()
            samples[index] = ((high shl 8) or low).toShort()
        }
        return samples
    }
}
