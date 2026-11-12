package com.voicedistorcer.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.voicedistorcer.R
import com.voicedistorcer.playback.AudioPreviewPlayer
import com.voicedistorcer.processing.AudioFileProcessor
import com.voicedistorcer.settings.VoiceSettingsStore
import com.voicedistorcer.sharing.ShareGateway
import java.io.File
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImportViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val importsDirectory = File(appContext.cacheDir, "imports")
    private val exportsDirectory = File(appContext.cacheDir, "exports")
    private val settingsStore = VoiceSettingsStore(appContext)

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    private val _isPreviewPlaying = MutableStateFlow(false)
    val isPreviewPlaying: StateFlow<Boolean> = _isPreviewPlaying.asStateFlow()

    private val _previewErrors = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val previewErrors: SharedFlow<Unit> = _previewErrors.asSharedFlow()

    private val previewPlayer = AudioPreviewPlayer(
        onPlayingChanged = { playing -> _isPreviewPlaying.value = playing },
        onError = { _previewErrors.tryEmit(Unit) },
    )

    private var workJob: Job? = null

    fun import(uri: Uri, mimeType: String?) {
        workJob?.cancel()
        stopPreview()
        (_uiState.value as? ImportUiState.Ready)?.file?.delete()
        _uiState.value = ImportUiState.Processing

        workJob = viewModelScope.launch {
            importsDirectory.mkdirs()
            exportsDirectory.mkdirs()
            val rawFile = File(importsDirectory, "import_${System.currentTimeMillis()}.media")
            var exportFile: File? = null

            try {
                withContext(Dispatchers.IO) {
                    val input = appContext.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Could not open the selected file")
                    input.use { source ->
                        rawFile.outputStream().use { target -> source.copyTo(target) }
                    }
                }
                if (rawFile.length() == 0L) {
                    throw IllegalStateException("Selected file is empty")
                }

                val resolvedMime = withContext(Dispatchers.IO) {
                    resolveMimeType(uri, mimeType)
                }
                if (resolvedMime?.startsWith("video/") == true) {
                    rawFile.delete()
                    _uiState.value = ImportUiState.Error(
                        appContext.getString(R.string.error_import_unsupported),
                    )
                    return@launch
                }

                val config = settingsStore.load().toDistortionConfig()
                val target = File(
                    exportsDirectory,
                    "anonymized_audio_${System.currentTimeMillis()}.m4a",
                )
                exportFile = target

                val durationMs = withContext(Dispatchers.IO) {
                    AudioFileProcessor.process(rawFile, target, config).durationMs
                }

                ensureActive()
                rawFile.delete()
                _uiState.value = ImportUiState.Ready(target, durationMs)
            } catch (cancelled: CancellationException) {
                rawFile.delete()
                exportFile?.delete()
                throw cancelled
            } catch (error: Exception) {
                rawFile.delete()
                exportFile?.delete()
                _uiState.value = ImportUiState.Error(
                    appContext.getString(R.string.error_import_failed),
                )
            }
        }
    }

    fun onMissingInput() {
        workJob?.cancel()
        stopPreview()
        (_uiState.value as? ImportUiState.Ready)?.file?.delete()
        _uiState.value = ImportUiState.Error(
            appContext.getString(R.string.error_import_missing),
        )
    }

    fun share(context: Context): Boolean {
        val state = _uiState.value as? ImportUiState.Ready ?: return false
        stopPreview()
        return ShareGateway.share(context, state.file)
    }

    fun togglePreview() {
        val state = _uiState.value as? ImportUiState.Ready ?: return
        previewPlayer.toggle(state.file)
    }

    fun stopPreview() {
        previewPlayer.stop()
    }

    override fun onCleared() {
        previewPlayer.release()
        workJob?.cancel()
        super.onCleared()
    }

    private fun resolveMimeType(uri: Uri, fallback: String?): String? {
        val fromResolver = runCatching {
            appContext.contentResolver.getType(uri)
        }.getOrNull()
        if (fromResolver != null && fromResolver != GENERIC_MIME) return fromResolver

        if (fallback != null && fallback != "*/*" && fallback != GENERIC_MIME) {
            return fallback
        }

        val name = displayName(uri) ?: uri.lastPathSegment ?: return null
        val extension = name.substringAfterLast('.', "").lowercase(Locale.US)
        if (extension.isEmpty()) return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    private fun displayName(uri: Uri): String? = runCatching {
        appContext.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()

    private companion object {
        const val GENERIC_MIME = "application/octet-stream"
    }
}
