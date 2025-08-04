package com.github.jibbo.norwegiantraining.log

import com.github.jibbo.norwegiantraining.data.Session

sealed class UiState {
    object Loading : UiState()
    class Loaded(val logs: List<Session>): UiState()
}