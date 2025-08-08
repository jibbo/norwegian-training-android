package com.github.jibbo.norwegiantraining.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.UserPreferencesRepo
import com.github.jibbo.norwegiantraining.domain.GetTodaySessionUseCase
import com.github.jibbo.norwegiantraining.domain.GetUsername
import com.github.jibbo.norwegiantraining.domain.MoveToNextPhaseDomainService
import com.github.jibbo.norwegiantraining.domain.Phase
import com.github.jibbo.norwegiantraining.domain.SkipPhaseUseCase
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
    private val getNextPhase: MoveToNextPhaseDomainService,
    private val getTodaySession: GetTodaySessionUseCase,
    private val skipPhase: SkipPhaseUseCase,
    private val getUsername: GetUsername,
    // TODO remove direct access to repos
    private val settingsRepository: UserPreferencesRepo,
) : ViewModel() {
    private var todaySession: Session = Session()

    private val events: MutableSharedFlow<UiCommands> = MutableSharedFlow()
    val uiEvents = events.asSharedFlow()

    private val states: MutableStateFlow<UiState> = MutableStateFlow(
        UiState(name = getUsername())
    )
    val uiStates = states.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            todaySession = getTodaySession()
        }
        //TODO this should be moved to datastore for Flow usage and avoid this workaround
        states.value = states.value.copy(
            name = getUsername(),
        )
    }

    fun mainButtonClicked() {
        viewModelScope.launch {
            if (states.value.isTimerRunning) {
                pauseTimer()
            } else if (states.value.remainingTimeOnPauseMillis > 0) {
                scheduleTimer(states.value.step, states.value.remainingTimeOnPauseMillis)
            } else {
                moveToNextPhase(getNextPhase())
            }
        }
    }

    fun showSkipButton() = states.value.step != Phase.COMPLETED

    fun showCountdown() = states.value.step != Phase.COMPLETED

    fun permissionGranted() {
        val oldValue = states.value
        if (oldValue.isTimerRunning && oldValue.targetTimeMillis > System.currentTimeMillis()) {
            publishEvent(UiCommands.SHOW_NOTIFICATION(oldValue.targetTimeMillis))
        }
    }

    fun skipClicked() {
        viewModelScope.launch {
            skipPhase()
            moveToNextPhase(getNextPhase())
        }
    }

    fun onTimerFinish() {
        viewModelScope.launch {
            moveToNextPhase(getNextPhase())
        }
    }

    fun shouldAnnouncePhase() = settingsRepository.getAnnouncePhase()
    fun shouldAnnouncePhaseDesc() = settingsRepository.getAnnouncePhaseDesc()

    fun settingsClicked() {
        publishEvent(UiCommands.SHOW_SETTINGS)
    }

    fun chartsClicked() {
        publishEvent(UiCommands.SHOW_CHARTS)
    }

    private fun moveToNextPhase(nextPhase: Phase) {
        when (nextPhase) {
            Phase.GET_READY -> showGetReady()
            Phase.COMPLETED -> showCompleted()
            else -> scheduleTimer(nextPhase, nextPhase.durationMillis!!) // I know it's not null
        }
    }

    private fun showGetReady() {
        states.value = UiState(
            step = Phase.GET_READY,
            isTimerRunning = false,
            targetTimeMillis = 0L,
            remainingTimeOnPauseMillis = 0L
        )
    }

    private fun showCompleted() {
        states.value = UiState(
            step = Phase.COMPLETED,
            isTimerRunning = false,
            targetTimeMillis = 0L,
            remainingTimeOnPauseMillis = 0L
        )
    }

    private fun pauseTimer() {
        val oldValue = states.value

        val remainingMillis =
            if (oldValue.isTimerRunning && oldValue.targetTimeMillis > System.currentTimeMillis()) {
                oldValue.targetTimeMillis - System.currentTimeMillis()
            } else {
                0L
            }

        states.value = UiState(oldValue.step, false, oldValue.targetTimeMillis, remainingMillis)
        publishEvent(UiCommands.PAUSE_ALARM)
    }

    private fun scheduleTimer(phase: Phase, duration: Long) {
        val newTargetTimeMillis = System.currentTimeMillis() + duration
        states.value = states.value.copy(
            step = phase,
            isTimerRunning = true,
            targetTimeMillis = newTargetTimeMillis,
            remainingTimeOnPauseMillis = 0L
        )
        publishEvent(UiCommands.START_ALARM(newTargetTimeMillis, states.value))
        ticking()
    }

    private fun ticking() {
        if (settingsRepository.getAnnounceCountdown()) {
            viewModelScope.launch {
                if (states.value.isTimerRunning) {
                    val remainingTime =
                        (System.currentTimeMillis() - states.value.targetTimeMillis) / 1000
                    Log.i("ticking", remainingTime.toString())
                    val speakState = SpeakState.Companion.from(remainingTime.toInt())
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

    sealed class UiCommands {
        object PAUSE_ALARM : UiCommands()
        object SHOW_SETTINGS : UiCommands()
        object SHOW_CHARTS : UiCommands()
        data class START_ALARM(val triggerTime: Long, val uiState: UiState) : UiCommands()
        data class SHOW_NOTIFICATION(val triggerTime: Long) : UiCommands()
        data class Speak(val speakState: SpeakState) : UiCommands()
    }
}
