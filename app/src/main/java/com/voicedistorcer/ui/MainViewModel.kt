package com.voicedistorcer.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicedistorcer.R
import com.voicedistorcer.playback.AudioPreviewPlayer
import com.voicedistorcer.processing.AudioEncoder
import com.voicedistorcer.processing.PcmFile
import com.voicedistorcer.processing.VoiceDistortionProcessor
import com.voicedistorcer.recording.RecordingEngine
import com.voicedistorcer.settings.VoicePreset
import com.voicedistorcer.settings.VoiceSettings
import com.voicedistorcer.settings.VoiceSettingsCodec
import com.voicedistorcer.settings.VoiceSettingsStore
import com.voicedistorcer.sharing.ShareGateway
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val settingsStore = VoiceSettingsStore(appContext)

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow(settingsStore.load())
    val settings: StateFlow<VoiceSettings> = _settings.asStateFlow()

    private val _isPreviewPlaying = MutableStateFlow(false)
    val isPreviewPlaying: StateFlow<Boolean> = _isPreviewPlaying.asStateFlow()

    private val _previewErrors = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val previewErrors: SharedFlow<Unit> = _previewErrors.asSharedFlow()

    private val _settingsTransferMessages = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val settingsTransferMessages: SharedFlow<Int> = _settingsTransferMessages.asSharedFlow()

    private val previewPlayer = AudioPreviewPlayer(
        onPlayingChanged = { playing -> _isPreviewPlaying.value = playing },
        onError = { _previewErrors.tryEmit(Unit) },
    )

    private var workJob: Job? = null
    private var outputFile: File? = null

    @Volatile
    private var recorderStarted = false

    @Volatile
    private var pendingStop = false

    fun startRecording() {
        if (workJob?.isActive == true) return

        stopPreview()
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

                val currentSettings = _settings.value
                val processed = withContext(Dispatchers.Default) {
                    VoiceDistortionProcessor(currentSettings.toDistortionConfig())
                        .process(pcm, RecordingEngine.SAMPLE_RATE)
                }
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
        stopPreview()
        return ShareGateway.share(context, file)
    }

    fun togglePreview() {
        val file = outputFile ?: return
        previewPlayer.toggle(file)
    }

    fun stopPreview() {
        previewPlayer.stop()
    }

    fun applyPreset(preset: VoicePreset) {
        updateSettings(VoiceSettings.forPreset(preset))
    }

    fun updateTone(semitones: Double) {
        updateSettings(_settings.value.withTone(semitones))
    }

    fun updateFormant(semitones: Double) {
        updateSettings(_settings.value.withFormant(semitones))
    }

    fun updateSpeed(speed: Double) {
        updateSettings(_settings.value.withSpeed(speed))
    }

    fun updateGrit(value: Double) {
        updateSettings(_settings.value.withGrit(value))
    }

    fun updateRobot(value: Double) {
        updateSettings(_settings.value.withRobot(value))
    }

    fun updateEcho(value: Double) {
        updateSettings(_settings.value.withEcho(value))
    }

    fun updateFan(value: Double) {
        updateSettings(_settings.value.withFan(value))
    }

    fun updateAdvancedSettings(settings: VoiceSettings) {
        updateSettings(settings)
    }

    fun exportSettings(uri: Uri) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                runCatching {
                    val contents = VoiceSettingsCodec.encode(_settings.value)
                    val stream = appContext.contentResolver.openOutputStream(uri)
                        ?: error("Unable to open export destination")
                    stream.use { it.write(contents.toByteArray(Charsets.UTF_8)) }
                }.isSuccess
            }
            _settingsTransferMessages.tryEmit(
                if (success) {
                    R.string.settings_export_success
                } else {
                    R.string.settings_export_failed
                },
            )
        }
    }

    fun importSettings(uri: Uri) {
        viewModelScope.launch {
            val imported = withContext(Dispatchers.IO) {
                runCatching {
                    val stream = appContext.contentResolver.openInputStream(uri)
                        ?: error("Unable to open settings file")
                    val contents = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    VoiceSettingsCodec.decode(contents)
                }
            }
            imported.onSuccess { updateSettings(it) }
            _settingsTransferMessages.tryEmit(
                if (imported.isSuccess) {
                    R.string.settings_import_success
                } else {
                    R.string.settings_import_failed
                },
            )
        }
    }

    private fun updateSettings(settings: VoiceSettings) {
        _settings.value = settings
        settingsStore.save(settings)
    }

    override fun onCleared() {
        previewPlayer.release()
        recorder.stop()
        workJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val TIMER_INTERVAL_MS = 200L
        const val BYTES_PER_SAMPLE = 2L
    }
}
