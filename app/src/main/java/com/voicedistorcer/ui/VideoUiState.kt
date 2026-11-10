package com.voicedistorcer.ui

import java.io.File

sealed interface VideoUiState {
    data object Idle : VideoUiState
    data object Starting : VideoUiState
    data class Recording(val elapsedMs: Long) : VideoUiState
    data object Processing : VideoUiState
    data class Ready(val durationMs: Long, val file: File) : VideoUiState
    data class Error(val message: String) : VideoUiState
}
