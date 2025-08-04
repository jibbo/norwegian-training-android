package com.github.jibbo.norwegiantraining.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.LogRepository
import com.github.jibbo.norwegiantraining.data.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LogViewModel @Inject constructor(
    private val logRepository: LogRepository
): ViewModel() {
    private val uiStates: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val uiState = uiStates.asStateFlow()

    init {
        viewModelScope.launch {
            uiStates.value = UiState.Loaded(logRepository.getSessionLogs().toList())
        }
    }
}