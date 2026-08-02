package com.github.jibbo.norwegiantraining.main

import android.app.ReviewManagerFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.domain.FitnessLevel
import com.github.jibbo.norwegiantraining.domain.PhaseName
import com.github.jibbo.norwegiantraining.domain.ProgressionResult
import com.github.jibbo.norwegiantraining.service.WorkoutTimerService
import com.github.jibbo.norwegiantraining.service.WorkoutTimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

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
            currentPhaseIndex = serviceState.currentPhaseIndex,
            totalPhases = serviceState.totalPhases,
            workoutName = serviceState.workoutName,
            showConfetti = serviceState.isCompleted && !currentState.showConfetti,
            isServiceBound = currentState.isServiceBound,
            progressionResult = serviceState.progressionResult
        )
    }

    fun onMainButtonClicked(){
        if (states.value.isCompleted) {
            closeWorkout()
        } else {
            mainButtonClicked()
        }

    }

    private fun mainButtonClicked() {
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
        states.value.step.name != PhaseName.COMPLETED

    fun skipClicked() {
        viewModelScope.launch {
            serviceBinder?.skipPhase()
        }
    }

    fun closeWorkout() {
        viewModelScope.launch {
            val progression = states.value.progressionResult
            serviceBinder?.closeWorkout()
            requestReviewIfApplicable()
            if (progression is ProgressionResult.LevelUp) {
                events.emit(UiCommands.LevelUp(progression.newLevel))
            } else {
                events.emit(UiCommands.CLOSE)
            }
        }
    }

    fun requestReviewIfApplicable() {
        viewModelScope.launch {
            val shownDates = settingsRepository.getReviewPromptShownDates()
            if (shownDates.size >= 3) return@launch

            val shouldShow = if (shownDates.isEmpty()) {
                true
            } else {
                val lastShown = shownDates.maxOrNull()!!
                val daysSinceLast = TimeUnit.MILLISECONDS.toDays(
                    System.currentTimeMillis() - lastShown
                )
                daysSinceLast >= 30
            }

            if (shouldShow) {
                try {
                    val manager = ReviewManagerFactory.create(
                        com.github.jibbo.norwegiantraining.NorwegianTrainingApp.appContext
                    )
                    val request = manager.requestReviewFlow()
                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val pendingIntent = task.result?.pendingIntent
                            pendingIntent?.let {
                                // We can't launch it from a ViewModel, so we emit a command
                                viewModelScope.launch {
                                    events.emit(UiCommands.RequestReview(it))
                                }
                            }
                        }
                        // Mark it shown regardless of success/failure
                        settingsRepository.addReviewPromptShownDate(Date(System.currentTimeMillis()))
                    }
                } catch (_: Exception) {
                    // App context unavailable or other error, mark as shown
                    settingsRepository.addReviewPromptShownDate(Date(System.currentTimeMillis()))
                }
            }
        }
    }

    fun debugShowConfetti() {
        states.value = states.value.copy(
            showConfetti = true
        )
    }

    fun debugCompleteWorkout() {
        viewModelScope.launch {
            serviceBinder?.debugCompleteWorkout()
        }
    }

    fun debugShowLevelUp() {
        viewModelScope.launch {
            events.emit(UiCommands.LevelUp(FitnessLevel.OCCASIONAL))
        }
    }

    sealed class UiCommands {
        object CLOSE : UiCommands()
        data class LevelUp(val newLevel: FitnessLevel) : UiCommands()
    }
}
