package com.github.jibbo.norwegiantraining.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.service.WorkoutTimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
internal class LogViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val workoutTimerManager: WorkoutTimerManager,
) : ViewModel() {
    private val uiStates: MutableStateFlow<UiState> = MutableStateFlow(UiState.Loading)
    val uiState = uiStates.asStateFlow()
    private val _activeSessionId = MutableStateFlow<Long?>(null)
    val activeSessionId = _activeSessionId.asStateFlow()

    private val _deleteBlocked = MutableStateFlow(false)
    val deleteBlocked = _deleteBlocked.asStateFlow()

    init {
        viewModelScope.launch {
            workoutTimerManager.getWorkoutTimerState().collect { state ->
                _activeSessionId.value = state.sessionId.takeUnless { state.isCompleted }
            }
        }
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

    fun refreshMonth(date: Date) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val from = calendar.time
            calendar.add(Calendar.MONTH, 1)
            val to = Date(calendar.timeInMillis - 1)
            val sessions = sessionRepository.getSessionsInRange(from, to)
            val month = Calendar.getInstance().apply { time = date }.get(Calendar.MONTH)

            val current = uiStates.value
            if (current is UiState.Loaded) {
                uiStates.value = UiState.Loaded(current.logs + (month to sessions))
            }
        }
    }

    fun deleteSession(session: Session, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (activeSessionId.value == session.id) {
                _deleteBlocked.value = true
                return@launch
            }
            sessionRepository.deleteSession(session.id)
            refreshMonth(session.date)
            onSuccess()
        }
    }

    fun dismissDeleteBlockedMessage() {
        _deleteBlocked.value = false
    }
}
