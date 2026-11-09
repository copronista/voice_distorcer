package com.voicedistorcer.playback

import android.media.AudioAttributes
import android.media.MediaPlayer
import java.io.File

class AudioPreviewPlayer(
    private val onPlayingChanged: (Boolean) -> Unit,
    private val onError: () -> Unit,
) {
    private var player: MediaPlayer? = null

    fun toggle(file: File) {
        if (player != null) {
            stop()
            return
        }

        val mediaPlayer = MediaPlayer()
        player = mediaPlayer
        try {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.setOnPreparedListener { prepared ->
                if (player === mediaPlayer) {
                    prepared.start()
                    onPlayingChanged(true)
                }
            }
            mediaPlayer.setOnCompletionListener { completed ->
                finish(completed)
            }
            mediaPlayer.setOnErrorListener { errored, _, _ ->
                finish(errored)
                onError()
                true
            }
            mediaPlayer.prepareAsync()
        } catch (error: Exception) {
            finish(mediaPlayer)
            onError()
        }
    }

    fun stop() {
        player?.let { finish(it) }
    }

    fun release() {
        stop()
    }

    private fun finish(mediaPlayer: MediaPlayer) {
        if (player === mediaPlayer) {
            player = null
            onPlayingChanged(false)
        }
        runCatching { mediaPlayer.stop() }
        mediaPlayer.release()
    }
}
