package com.voicedistorcer.ui

import java.io.File

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object Processing : ImportUiState
    data class Ready(
        val file: File,
        val durationMs: Long,
    ) : ImportUiState
    data class Error(val message: String) : ImportUiState
}
