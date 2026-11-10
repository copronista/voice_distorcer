package com.voicedistorcer.ui

import java.io.File

enum class ImportedMediaKind {
    AUDIO,
    VIDEO,
}

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object Processing : ImportUiState
    data class Ready(
        val file: File,
        val durationMs: Long,
        val kind: ImportedMediaKind,
    ) : ImportUiState
    data class Error(val message: String) : ImportUiState
}
