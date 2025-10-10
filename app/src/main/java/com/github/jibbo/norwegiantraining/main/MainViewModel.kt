package com.github.jibbo.norwegiantraining.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.domain.GetTodaySessionUseCase
import com.github.jibbo.norwegiantraining.domain.GetUsername
import com.github.jibbo.norwegiantraining.domain.MoveToNextPhaseDomainService
import com.github.jibbo.norwegiantraining.domain.Phase
import com.github.jibbo.norwegiantraining.domain.PhaseEndedUseCase
import com.github.jibbo.norwegiantraining.domain.PhaseName
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
    private val phaseEnded: PhaseEndedUseCase,
    private val skipPhase: SkipPhaseUseCase,
    private val getUsername: GetUsername,
    // TODO remove direct access to repos
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private var workoutId = 0L
    private var todaySession: Session = Session()

    // TODO move to foreground service
    private var currentStep: Int = 0

    private val events: MutableSharedFlow<UiCommands> = MutableSharedFlow()
    val uiEvents = events.asSharedFlow()

    private val states: MutableStateFlow<UiState> = MutableStateFlow(
        UiState(name = getUsername())
    )
    val uiStates = states.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            todaySession = getTodaySession()
            states.value = states.value.copy(
                //TODO this should be moved to datastore for Flow usage and avoid this workaround
                name = getUsername(),
            )
        }
    }

    fun mainButtonClicked() {
        viewModelScope.launch {
            if (states.value.isTimerRunning) {
                pauseTimer()
            } else if (states.value.remainingTimeOnPauseMillis > 0) {
                scheduleTimer(states.value.step, states.value.remainingTimeOnPauseMillis)
            } else if (states.value.step.durationMillis > 0L) {
                scheduleTimer(states.value.step, states.value.step.durationMillis)
            } else {
                showNextPhase(getNextPhase(workoutId, currentStep))
            }
        }
    }

    fun showSkipButton() =
        states.value.step.name != PhaseName.COMPLETED && states.value.step.name != PhaseName.GET_READY

    fun showCountdown() =
        states.value.step.name != PhaseName.COMPLETED && states.value.step.name != PhaseName.GET_READY

    fun permissionGranted() {
        val oldValue = states.value
        if (oldValue.isTimerRunning && oldValue.targetTimeMillis > System.currentTimeMillis()) {
            publishEvent(UiCommands.SHOW_NOTIFICATION(oldValue.targetTimeMillis))
        }
    }

    fun skipClicked() {
        viewModelScope.launch {
            todaySession = skipPhase()
            showNextPhase(getNextPhase(workoutId, currentStep))
        }
    }

    fun onTimerFinish() {
        viewModelScope.launch {
            todaySession = phaseEnded()
            showNextPhase(getNextPhase(workoutId, currentStep))
        }
    }

    fun closeWorkout() {
        viewModelScope.launch {
            events.emit(UiCommands.CLOSE)
        }
    }

    private fun showNextPhase(nextPhase: Phase) {
        when (nextPhase.name) {
            PhaseName.GET_READY -> showGetReady()
            PhaseName.COMPLETED -> showCompleted()
            else -> scheduleTimer(nextPhase, nextPhase.durationMillis)
        }
        currentStep++
    }

    private fun showGetReady() {
        states.value = UiState(
            step = Phase(PhaseName.GET_READY, 0L),
            isTimerRunning = false,
            targetTimeMillis = 0L,
            remainingTimeOnPauseMillis = 0L
        )
    }

    private fun showCompleted() {
        states.value = UiState(
            step = Phase(PhaseName.COMPLETED, 0L),
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
        if (settingsRepository.getShowTimerNotification()) {
            publishEvent(UiCommands.START_ALARM(newTargetTimeMillis, states.value))
        }
        if (settingsRepository.getAnnouncePhase()) {
            UiCommands.Speak(SpeakState.Message(phase.name.message()), flush = true)
        }
        if (settingsRepository.getAnnouncePhaseDesc()) {
            UiCommands.Speak(SpeakState.Message(phase.name.description()))
        }
        ticking()
    }

    private fun ticking() {
        if (settingsRepository.getAnnounceCountdown()) {
            viewModelScope.launch {
                if (states.value.isTimerRunning) {
                    val remainingTime =
                        (System.currentTimeMillis() - states.value.targetTimeMillis) / 1000
                    val speakState = SpeakState.Companion.fromSeconds(remainingTime.toInt())
                    if (speakState != SpeakState.Nothing) {
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

    fun setId(id: Long) {
        if (id == -1L) {
            closeWorkout()
            return
        }
        workoutId = id
        refresh()
    }

    sealed class UiCommands {
        object PAUSE_ALARM : UiCommands()
        object CLOSE : UiCommands()
        data class START_ALARM(val triggerTime: Long, val uiState: UiState) : UiCommands()
        data class SHOW_NOTIFICATION(val triggerTime: Long) : UiCommands()
        data class Speak(val speakState: SpeakState, val flush: Boolean = false) : UiCommands()
    }
}
