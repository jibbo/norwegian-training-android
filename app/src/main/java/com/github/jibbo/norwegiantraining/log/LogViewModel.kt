package com.github.jibbo.norwegiantraining.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
internal class LogViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val uiStates: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val uiState = uiStates.asStateFlow()

    init {
        viewModelScope.launch {
            uiStates.value = UiState.Loaded(prepareSession())
        }
    }

    private suspend fun prepareSession(): Map<Int, List<Session>> {
        val sessions = sessionRepository.getSessions().toList()
        val sessionsByMonth = HashMap<Int, MutableList<Session>>()
        val calendar = Calendar.getInstance()
        sessions.forEach { session ->
            calendar.timeInMillis = session.date.time
            val month = calendar.get(Calendar.MONTH)
            sessionsByMonth.getOrPut(month) { mutableListOf() }.add(session)
        }
        return sessionsByMonth
    }
}
