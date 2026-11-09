package com.voicedistorcer.ui

import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicedistorcer.R
import com.voicedistorcer.processing.AudioEncoder
import com.voicedistorcer.processing.PcmFile
import com.voicedistorcer.processing.VoiceDistortionProcessor
import com.voicedistorcer.recording.RecordingEngine
import com.voicedistorcer.sharing.ShareGateway
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val recordingsDirectory = File(appContext.cacheDir, "recordings")
    private val exportsDirectory = File(appContext.cacheDir, "exports")

    private val recorder = RecordingEngine()
    private val processor = VoiceDistortionProcessor()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var workJob: Job? = null
    private var outputFile: File? = null

    @Volatile
    private var recorderStarted = false

    @Volatile
    private var pendingStop = false

    fun startRecording() {
        if (workJob?.isActive == true) return

        recordingsDirectory.mkdirs()
        exportsDirectory.mkdirs()

        val rawFile = File(recordingsDirectory, "raw_${System.currentTimeMillis()}.pcm")
        val startedAt = SystemClock.elapsedRealtime()
        recorderStarted = false
        pendingStop = false
        _uiState.value = UiState.Recording(0)

        workJob = viewModelScope.launch {
            val timerJob = launch {
                while (isActive) {
                    if (_uiState.value is UiState.Recording) {
                        _uiState.value =
                            UiState.Recording(SystemClock.elapsedRealtime() - startedAt)
                    }
                    delay(TIMER_INTERVAL_MS)
                }
            }

            try {
                recorder.recordTo(rawFile) {
                    recorderStarted = true
                    if (pendingStop) {
                        recorder.stop()
                    }
                }
            } catch (cancelled: CancellationException) {
                timerJob.cancel()
                throw cancelled
            } catch (error: Exception) {
                timerJob.cancel()
                _uiState.value = UiState.Error(appContext.getString(R.string.error_recording_failed))
                return@launch
            }

            timerJob.cancel()

            if (rawFile.length() < BYTES_PER_SAMPLE) {
                rawFile.delete()
                _uiState.value = UiState.Error(appContext.getString(R.string.error_empty_recording))
                return@launch
            }

            _uiState.value = UiState.Processing
            try {
                val pcm = withContext(Dispatchers.IO) { PcmFile.read(rawFile) }
                rawFile.delete()

                val processed = withContext(Dispatchers.Default) { processor.process(pcm) }
                if (processed.isEmpty()) {
                    throw IllegalStateException("Processed clip is empty")
                }

                val exportFile = File(exportsDirectory, "anonymized_${System.currentTimeMillis()}.m4a")
                withContext(Dispatchers.IO) {
                    AudioEncoder.encodeToM4a(processed, RecordingEngine.SAMPLE_RATE, exportFile)
                }

                outputFile = exportFile
                val durationMs = processed.size * 1_000L / RecordingEngine.SAMPLE_RATE
                _uiState.value = UiState.Ready(durationMs)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = UiState.Error(appContext.getString(R.string.error_processing_failed))
            }
        }
    }

    fun stopRecording() {
        if (!recorderStarted) {
            pendingStop = true
        }
        recorder.stop()
    }

    fun share(context: Context): Boolean {
        val file = outputFile ?: return false
        return ShareGateway.share(context, file)
    }

    override fun onCleared() {
        recorder.stop()
        workJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val TIMER_INTERVAL_MS = 200L
        const val BYTES_PER_SAMPLE = 2L
    }
}
