package com.voicedistorcer.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicedistorcer.R
import com.voicedistorcer.settings.VoicePreset
import com.voicedistorcer.settings.VoiceSettings
import com.voicedistorcer.settings.VoiceSettingsCodec
import com.voicedistorcer.settings.VoiceSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val settingsStore = VoiceSettingsStore(appContext)

    private val _settings = MutableStateFlow(settingsStore.load())
    val settings: StateFlow<VoiceSettings> = _settings.asStateFlow()

    private val _settingsTransferMessages = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val settingsTransferMessages: SharedFlow<Int> = _settingsTransferMessages.asSharedFlow()

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
}
