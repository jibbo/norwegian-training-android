package com.github.jibbo.norwegiantraining.service

import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import com.github.jibbo.norwegiantraining.domain.MoveToNextPhaseDomainService
import com.github.jibbo.norwegiantraining.domain.WorkoutToPhasesConverter
import com.github.jibbo.norwegiantraining.domain.displayLabel
import com.github.jibbo.norwegiantraining.domain.WorkoutNotFoundException
import com.github.jibbo.norwegiantraining.domain.Phase
import com.github.jibbo.norwegiantraining.domain.PhaseName
import com.github.jibbo.norwegiantraining.domain.SkipPhaseUseCase
import com.github.jibbo.norwegiantraining.domain.WorkoutCompletedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutTimerStateManager @Inject constructor(
    private val persistence: TimerStatePersistence,
    private val workoutRepository: WorkoutRepository,
    private val moveToNextPhase: MoveToNextPhaseDomainService,
    private val workoutCompletedUseCase: WorkoutCompletedUseCase,
    private val skipPhaseUseCase: SkipPhaseUseCase,
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val getTodaySession: com.github.jibbo.norwegiantraining.domain.GetTodaySessionUseCase,
): WorkoutTimerManager {
    private val _state = MutableStateFlow(WorkoutTimerState())
    val state: StateFlow<WorkoutTimerState> = _state.asStateFlow()
    private val commandMutex = Mutex()

    override fun getWorkoutTimerState(): StateFlow<WorkoutTimerState> = _state.asStateFlow()

    suspend fun initialize() {
        val savedState = persistence.loadState()
        if (savedState == null) {
            persistence.clearState()
            return
        }

        val workout = workoutRepository.getById(savedState.workoutId)
        if (workout == null) {
            persistence.clearState()
            _state.value = WorkoutTimerState()
            return
        }

        val phases = WorkoutToPhasesConverter.convert(workout).getOrElse {
            persistence.clearState()
            _state.value = WorkoutTimerState()
            return
        }

        val savedSession = savedState.sessionId?.let { sessionRepository.getSession(it) }
        val session = savedSession?.takeIf { it.workoutId == savedState.workoutId }
            ?: findOrCreateLegacySession(workout)
        _state.value = savedState.copy(
            workoutName = workout.displayLabel(),
            totalPhases = phases.size,
            sessionId = session.id,
        )
        if (savedState.sessionId != session.id) persistence.saveState(_state.value)
    }

    suspend fun startWorkout(workoutId: Long): Result<Unit> {
        return commandMutex.withLock {
            val currentState = _state.value
            if (currentState.workoutId == workoutId && !currentState.isCompleted && currentState.sessionId != null) {
                return@withLock Result.success(Unit)
            }

            val workout = workoutRepository.getById(workoutId)
                ?: return@withLock Result.failure(WorkoutNotFoundException(workoutId))
            val phases = WorkoutToPhasesConverter.convert(workout)
                .getOrElse { return@withLock Result.failure(it) }
            val session = getSession(workout)

            val initialPhase =
                Phase(PhaseName.GET_READY, WorkoutToPhasesConverter.GET_READY_COUNTDOWN_DURATION)
            val newState = WorkoutTimerState(
                workoutId = workoutId,
                sessionId = session.id,
                workoutName = workout.displayLabel(),
                currentPhaseIndex = 0,
                totalPhases = phases.size,
                currentPhase = initialPhase,
                targetTimeMillis = 0L,
                isTimerRunning = false,
                remainingTimeOnPauseMillis = 0L,
                isCompleted = false
            )

            updateState(newState)
            Result.success(Unit)
        }
    }

    suspend fun startTimer() {
        commandMutex.withLock { startTimerLocked() }
    }

    private suspend fun startTimerLocked() {
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
        commandMutex.withLock { pauseTimerLocked() }
    }

    private suspend fun pauseTimerLocked() {
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

    suspend fun moveToNextPhase(expectedPhaseIndex: Int? = null): Result<Unit> {
        return commandMutex.withLock { moveToNextPhaseLocked(expectedPhaseIndex) }
    }

    private suspend fun moveToNextPhaseLocked(expectedPhaseIndex: Int? = null): Result<Unit> {
        val currentState = _state.value
        if (currentState.isCompleted || currentState.sessionId == null) return Result.success(Unit)
        if (expectedPhaseIndex != null && currentState.currentPhaseIndex != expectedPhaseIndex) {
            return Result.failure(StalePhaseTransitionException)
        }

        val nextPhase = moveToNextPhase(currentState.workoutId, currentState.currentPhaseIndex)
            .getOrElse { return Result.failure(it) }
        val nextIndex = currentState.currentPhaseIndex + 1

        val isCompleted = nextPhase.name == PhaseName.COMPLETED

        val progressionResult = if (isCompleted) {
            workoutCompletedUseCase(currentState.workoutId, currentState.sessionId).progression
        } else null

        updateState(
            currentState.copy(
                currentPhaseIndex = nextIndex,
                currentPhase = nextPhase,
                targetTimeMillis = 0L,
                isTimerRunning = false,
                remainingTimeOnPauseMillis = 0L,
                isCompleted = isCompleted,
                progressionResult = progressionResult
            )
        )
        return Result.success(Unit)
    }

    suspend fun skipPhase(expectedPhaseIndex: Int? = null): Result<Unit> {
        return commandMutex.withLock {
            val currentState = _state.value
            if (currentState.sessionId == null || currentState.isCompleted) return@withLock Result.success(Unit)
            if (expectedPhaseIndex != null && currentState.currentPhaseIndex != expectedPhaseIndex) {
                return@withLock Result.failure(StalePhaseTransitionException)
            }
            skipPhaseUseCase(currentState.workoutId, currentState.sessionId)
            moveToNextPhaseLocked()
        }
    }

    suspend fun closeWorkout() {
        commandMutex.withLock {
            updateState(WorkoutTimerState())
            persistence.clearState()
        }
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
    fun shouldAnnounceOneMinute(): Boolean = settingsRepository.getAnnounceOneMinute()
    fun shouldAnnouncePause(): Boolean = settingsRepository.getAnnouncePause()

    private suspend fun updateState(newState: WorkoutTimerState) {
        _state.value = newState
        persistence.saveState(newState)
    }

    private suspend fun getSession(workout: com.github.jibbo.norwegiantraining.data.Workout) =
        getTodaySession.createSession(workout)

    private suspend fun findOrCreateLegacySession(workout: com.github.jibbo.norwegiantraining.data.Workout) =
        sessionRepository.getNormalSessionForWorkoutInRange(workout.id, startOfToday(), endOfToday())
            ?: sessionRepository.getLegacyNormalSessionInRange(workout.name, workout.totalTime.toLong(), startOfToday(), endOfToday())
            ?: getSession(workout)

    private fun startOfToday(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.time
    }

    private fun endOfToday(): Date = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); add(Calendar.MILLISECOND, -1)
    }.time
}

object StalePhaseTransitionException : Exception()
