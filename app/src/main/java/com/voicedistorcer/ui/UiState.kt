package com.voicedistorcer.ui

sealed interface UiState {
    data object Idle : UiState
    data class Recording(val elapsedMs: Long) : UiState
    data object Processing : UiState
    data class Ready(val durationMs: Long) : UiState
    data class Error(val message: String) : UiState
}
