package com.github.jibbo.norwegiantraining.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AlarmViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {

    fun alarmReceived() {
        viewModelScope.launch {
            val session = sessionRepository.getTodaySession()
            session?.let {
                sessionRepository.upsertSession(session.copy(phasesEnded = session.phasesEnded + 1))
            }
            // TODO new phase
        }
    }
}
