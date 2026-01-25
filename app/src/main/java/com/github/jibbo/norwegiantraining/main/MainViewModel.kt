package com.github.jibbo.norwegiantraining.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.domain.PhaseName
import com.github.jibbo.norwegiantraining.service.WorkoutTimerService
import com.github.jibbo.norwegiantraining.service.WorkoutTimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private var serviceBinder: WorkoutTimerService? = null

    private val events: MutableSharedFlow<UiCommands> = MutableSharedFlow()
    val uiEvents = events.asSharedFlow()

    private val states: MutableStateFlow<UiState> = MutableStateFlow(
        UiState(workoutName = "")
    )
    val uiStates = states.asStateFlow()

    fun bindToService(binder: WorkoutTimerService) {
        serviceBinder = binder
        states.value = states.value.copy(isServiceBound = true)
    }

    fun unbind() {
        serviceBinder = null
        states.value = states.value.copy(isServiceBound = false)
    }

    fun updateFromService(serviceState: WorkoutTimerState) {
        val currentState = states.value
        states.value = UiState(
            step = serviceState.currentPhase,
            isTimerRunning = serviceState.isTimerRunning,
            targetTimeMillis = serviceState.targetTimeMillis,
            remainingTimeOnPauseMillis = serviceState.remainingTimeOnPauseMillis,
            workoutName = serviceState.workoutName,
            showConfetti = serviceState.isCompleted && !currentState.showConfetti,
            isServiceBound = currentState.isServiceBound
        )
    }

    fun mainButtonClicked() {
        viewModelScope.launch {
            serviceBinder?.let { service ->
                if (states.value.isTimerRunning) {
                    service.pauseTimer()
                } else if (states.value.remainingTimeOnPauseMillis > 0 || states.value.step.durationMillis > 0) {
                    service.startTimer()
                } else {
                    // Handle GET_READY phase (duration = 0) - advance to first real phase
                    // automatically starts the timer for the next phase
                    service.advanceToNextPhase()
                }
            }
        }
    }

    fun showSkipButton() =
        states.value.step.name != PhaseName.COMPLETED && states.value.step.name != PhaseName.GET_READY

    fun showCountdown() =
        states.value.step.name != PhaseName.COMPLETED && states.value.step.name != PhaseName.GET_READY

    fun skipClicked() {
        viewModelScope.launch {
            serviceBinder?.skipPhase()
        }
    }

    fun closeWorkout() {
        viewModelScope.launch {
            serviceBinder?.closeWorkout()
            events.emit(UiCommands.CLOSE)
        }
    }

    fun debugShowConfetti() {
        states.value = states.value.copy(
            showConfetti = true
        )
    }

    sealed class UiCommands {
        object CLOSE : UiCommands()
    }
}
