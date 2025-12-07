package com.github.jibbo.norwegiantraining.service

import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import com.github.jibbo.norwegiantraining.domain.GetTodaySessionUseCase
import com.github.jibbo.norwegiantraining.domain.MoveToNextPhaseDomainService
import com.github.jibbo.norwegiantraining.domain.Phase
import com.github.jibbo.norwegiantraining.domain.PhaseEndedUseCase
import com.github.jibbo.norwegiantraining.domain.PhaseName
import com.github.jibbo.norwegiantraining.domain.SkipPhaseUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutTimerStateManager @Inject constructor(
    private val persistence: TimerStatePersistence,
    private val workoutRepository: WorkoutRepository,
    private val moveToNextPhase: MoveToNextPhaseDomainService,
    private val phaseEndedUseCase: PhaseEndedUseCase,
    private val skipPhaseUseCase: SkipPhaseUseCase,
    private val getTodaySessionUseCase: GetTodaySessionUseCase,
    private val settingsRepository: SettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(WorkoutTimerState())
    val state: StateFlow<WorkoutTimerState> = _state.asStateFlow()

    suspend fun initialize() {
        val savedState = persistence.loadState()
        if (savedState != null) {
            _state.value = savedState
        }
    }

    suspend fun startWorkout(workoutId: Long) {
        val workout = workoutRepository.getById(workoutId) ?: return

        val initialPhase = Phase(PhaseName.GET_READY, 0L)
        val newState = WorkoutTimerState(
            workoutId = workoutId,
            workoutName = workout.name,
            currentPhaseIndex = 0,
            currentPhase = initialPhase,
            targetTimeMillis = 0L,
            isTimerRunning = false,
            remainingTimeOnPauseMillis = 0L,
            isCompleted = false
        )

        updateState(newState)
    }

    suspend fun startTimer() {
        val currentState = _state.value

        val duration = if (currentState.remainingTimeOnPauseMillis > 0) {
            currentState.remainingTimeOnPauseMillis
        } else {
            currentState.currentPhase.durationMillis
        }

        val targetTime = System.currentTimeMillis() + duration

        updateState(
            currentState.copy(
                isTimerRunning = true,
                targetTimeMillis = targetTime,
                remainingTimeOnPauseMillis = 0L
            )
        )
    }

    suspend fun pauseTimer() {
        val currentState = _state.value
        if (!currentState.isTimerRunning) return

        val remainingTime =
            (currentState.targetTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L)

        updateState(
            currentState.copy(
                isTimerRunning = false,
                remainingTimeOnPauseMillis = remainingTime,
                targetTimeMillis = 0L
            )
        )
    }

    suspend fun moveToNextPhase() {
        val currentState = _state.value

        phaseEndedUseCase()

        val nextPhase = moveToNextPhase(currentState.workoutId, currentState.currentPhaseIndex)
        val nextIndex = currentState.currentPhaseIndex + 1

        val isCompleted = nextPhase.name == PhaseName.COMPLETED

        updateState(
            currentState.copy(
                currentPhaseIndex = nextIndex,
                currentPhase = nextPhase,
                targetTimeMillis = 0L,
                isTimerRunning = false,
                remainingTimeOnPauseMillis = 0L,
                isCompleted = isCompleted
            )
        )
    }

    suspend fun skipPhase() {
        skipPhaseUseCase()
        moveToNextPhase()
    }

    suspend fun closeWorkout() {
        updateState(WorkoutTimerState())
        persistence.clearState()
    }

    fun getRemainingTimeMillis(): Long {
        val currentState = _state.value
        return if (currentState.isTimerRunning) {
            (currentState.targetTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        } else {
            currentState.remainingTimeOnPauseMillis
        }
    }

    fun shouldAnnouncePhase(): Boolean = settingsRepository.getAnnouncePhase()
    fun shouldAnnouncePhaseDesc(): Boolean = settingsRepository.getAnnouncePhaseDesc()
    fun shouldAnnounceCountdown(): Boolean = settingsRepository.getAnnounceCountdown()

    private fun updateState(newState: WorkoutTimerState) {
        _state.value = newState
        scope.launch {
            persistence.saveState(newState)
        }
    }
}
