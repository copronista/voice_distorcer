package com.voicedistorcer.ui

import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicedistorcer.R
import com.voicedistorcer.processing.VideoAudioProcessor
import com.voicedistorcer.settings.VoiceSettingsStore
import com.voicedistorcer.sharing.ShareGateway
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val recordingsDirectory = File(appContext.cacheDir, "video_recordings")
    private val exportsDirectory = File(appContext.cacheDir, "exports")
    private val settingsStore = VoiceSettingsStore(appContext)

    private val _uiState = MutableStateFlow<VideoUiState>(VideoUiState.Idle)
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    private var workJob: Job? = null
    private var timerJob: Job? = null
    private var outputFile: File? = null

    fun newRecordingFile(): File {
        recordingsDirectory.mkdirs()
        return File(recordingsDirectory, "raw_video_${System.currentTimeMillis()}.mp4")
    }

    fun onRecordingStarting() {
        if (workJob?.isActive == true) return

        outputFile?.delete()
        outputFile = null
        timerJob?.cancel()
        _uiState.value = VideoUiState.Starting
    }

    fun onRecordingStarted() {
        timerJob?.cancel()
        val startedAt = SystemClock.elapsedRealtime()
        _uiState.value = VideoUiState.Recording(0)
        timerJob = viewModelScope.launch {
            while (isActive) {
                if (_uiState.value is VideoUiState.Recording) {
                    _uiState.value = VideoUiState.Recording(
                        SystemClock.elapsedRealtime() - startedAt,
                    )
                }
                delay(TIMER_INTERVAL_MS)
            }
        }
    }

    fun onRecordingFinalized(rawFile: File) {
        timerJob?.cancel()
        timerJob = null

        if (!rawFile.exists() || rawFile.length() == 0L) {
            rawFile.delete()
            _uiState.value = VideoUiState.Error(appContext.getString(R.string.error_video_empty))
            return
        }
        processVideo(rawFile)
    }

    fun onRecordingError(message: String) {
        timerJob?.cancel()
        timerJob = null
        _uiState.value = VideoUiState.Error(message)
    }

    fun onCameraError(message: String) {
        when (_uiState.value) {
            VideoUiState.Idle, VideoUiState.Starting, is VideoUiState.Recording -> {
                timerJob?.cancel()
                timerJob = null
                _uiState.value = VideoUiState.Error(message)
            }

            else -> Unit
        }
    }

    fun share(context: Context): Boolean {
        val file = outputFile ?: return false
        return ShareGateway.share(
            context = context,
            file = file,
            mimeType = "video/*",
            chooserTitleRes = R.string.share_video_chooser_title,
        )
    }

    private fun processVideo(rawFile: File) {
        if (workJob?.isActive == true) return

        _uiState.value = VideoUiState.Processing
        workJob = viewModelScope.launch {
            exportsDirectory.mkdirs()
            val exportFile = File(
                exportsDirectory,
                "anonymized_video_${System.currentTimeMillis()}.mp4",
            )
            try {
                val config = settingsStore.load().toDistortionConfig()
                val result = withContext(Dispatchers.IO) {
                    VideoAudioProcessor.process(rawFile, exportFile, config)
                }
                ensureActive()
                rawFile.delete()
                outputFile = exportFile
                _uiState.value = VideoUiState.Ready(result.durationMs, exportFile)
            } catch (cancelled: CancellationException) {
                rawFile.delete()
                exportFile.delete()
                throw cancelled
            } catch (error: Exception) {
                rawFile.delete()
                exportFile.delete()
                outputFile = null
                _uiState.value = VideoUiState.Error(
                    appContext.getString(R.string.error_video_processing_failed),
                )
            }
        }
    }

    override fun onCleared() {
        workJob?.cancel()
        timerJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val TIMER_INTERVAL_MS = 200L
    }
}
