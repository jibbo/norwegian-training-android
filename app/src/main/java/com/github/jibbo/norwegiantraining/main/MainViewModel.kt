package com.github.jibbo.norwegiantraining.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.UserPreferencesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: UserPreferencesRepo,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private var currentStep = 0
    private var todaySession: Session = Session()

    private val events: MutableSharedFlow<UiCommands> = MutableSharedFlow()
    val uiEvents = events.asSharedFlow()

    private val states: MutableStateFlow<UiState> = MutableStateFlow(
        UiState(
            name = settingsRepository.getUserName() ?: ""
        )
    )
    val uiStates = states.asStateFlow()

    init {
        viewModelScope.launch {
            val fetchedSession = sessionRepository.getTodaySession()
            if (fetchedSession != null) {
                todaySession = fetchedSession
            } else {
                updateTodaySession(todaySession)
            }
        }
    }

    fun refresh() {
        states.value = uiStates.value.copy(
            name = settingsRepository.getUserName() ?: ""
        )
    }

    fun mainButtonClicked() {
        val oldValue = states.value
        if (currentStep > 9 && oldValue.isTimerRunning) {
            states.value = UiState(currentStep, false, 0L, 0L)
            events.tryEmit(UiCommands.STOP_ALARM)
        } else if (currentStep > 9) {
            currentStep = 0
            scheduleTimer()
        } else if (oldValue.isTimerRunning) {
            stopTimer()
        } else {
            scheduleTimer()
            updateTodaySession(todaySession)
        }
    }

    fun showSkipButton() = currentStep >= 0 && currentStep < 10

    fun showCountdown() = currentStep >= 0 && currentStep < 10

    fun permissionGranted() {
        val oldValue = states.value
        if (oldValue.isTimerRunning && oldValue.targetTimeMillis > System.currentTimeMillis()) {
            publishEvent(UiCommands.SHOW_NOTIFICATION(oldValue.targetTimeMillis))
        }
    }

    fun skipClicked() {
        updateTodaySession(todaySession.copy(skipCount = todaySession.skipCount + 1))
        currentStep++
        if (currentStep < 9) {
            scheduleTimer()
        } else {
            mainButtonClicked()
        }
    }

    fun onTimerFinish() {
        currentStep++
        states.value = UiState(
            step = currentStep,
            isTimerRunning = false,
            targetTimeMillis = 0L,
            remainingTimeOnPauseMillis = 0L
        )
    }

    fun shouldAnnouncePhase() = settingsRepository.getAnnouncePhase()
    fun shouldAnnouncePhaseDesc() = settingsRepository.getAnnouncePhaseDesc()

    fun settingsClicked() {
        publishEvent(UiCommands.SHOW_SETTINGS)
    }

    fun chartsClicked() {
        publishEvent(UiCommands.SHOW_CHARTS)
    }

    private fun updateTodaySession(session: Session) {
        viewModelScope.launch {
            val id = sessionRepository.upsertSession(session)
            todaySession.copy(id = id)
        }
    }

    private fun stopTimer() {
        val oldValue = states.value

        val remainingMillis =
            if (oldValue.isTimerRunning && oldValue.targetTimeMillis > System.currentTimeMillis()) {
                oldValue.targetTimeMillis - System.currentTimeMillis()
            } else {
                0L
            }

        states.value = UiState(oldValue.step, false, oldValue.targetTimeMillis, remainingMillis)
        publishEvent(UiCommands.STOP_ALARM)
    }

    private fun scheduleTimer() {
        val oldValue = states.value
        val newTargetTimeMillis: Long

        if (!oldValue.isTimerRunning && oldValue.remainingTimeOnPauseMillis > 0L && oldValue.step == currentStep) {
            // resume from pause
            newTargetTimeMillis = System.currentTimeMillis() + oldValue.remainingTimeOnPauseMillis
            states.value = UiState(currentStep, true, newTargetTimeMillis, 0L)
        } else {
            newTargetTimeMillis = getNextAlarmTime()
            states.value = UiState(currentStep, true, newTargetTimeMillis, 0L)
        }
        publishEvent(UiCommands.START_ALARM(newTargetTimeMillis, states.value))

        ticking()
    }

    private fun ticking() {
        if (settingsRepository.getAnnounceCountdown()) {
            viewModelScope.launch {
                if (states.value.isTimerRunning) {
                    val remainingTime =
                        ((System.currentTimeMillis() - states.value.targetTimeMillis) / 1000).toInt()
                    Log.i("ticking", remainingTime.toString())
                    val speakState = SpeakState.Companion.from(remainingTime)
                    if (speakState != SpeakState.NOTHING) {
                        publishEvent(UiCommands.Speak(speakState))
                    }
                    delay(1000)
                    ticking()
                }
            }
        }
    }

    private fun publishEvent(uiCommand: UiCommands) {
        viewModelScope.launch {
            events.emit(uiCommand)
        }
    }


    private fun getNextAlarmTime(): Long {
        val durationMillis = when (currentStep) {
            0 -> 10 * 60 * 1000
            9 -> 5 * 60 * 1000
            else -> 4 * 60 * 1000
        }
        return System.currentTimeMillis() + durationMillis
    }

    sealed class UiCommands {
        object STOP_ALARM : UiCommands()
        object SHOW_SETTINGS : UiCommands()
        object SHOW_CHARTS : UiCommands()
        data class START_ALARM(val triggerTime: Long, val uiState: UiState) : UiCommands()
        data class SHOW_NOTIFICATION(val triggerTime: Long) : UiCommands()
        data class Speak(val speakState: SpeakState) : UiCommands()
    }
}
