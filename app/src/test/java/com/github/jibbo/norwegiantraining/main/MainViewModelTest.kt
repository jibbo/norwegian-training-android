package com.github.jibbo.norwegiantraining.main

import com.github.jibbo.norwegiantraining.domain.FitnessLevel
import com.github.jibbo.norwegiantraining.domain.Phase
import com.github.jibbo.norwegiantraining.domain.PhaseName
import com.github.jibbo.norwegiantraining.domain.ProgressionResult
import com.github.jibbo.norwegiantraining.service.WorkoutTimerService
import com.github.jibbo.norwegiantraining.service.WorkoutTimerState
import com.github.jibbo.norwegiantraining.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun mainButtonStartsPausesAndAdvancesThroughBinder() = runTest {
        val binder = RecordingService()
        val viewModel = MainViewModel()
        viewModel.bindToService(binder)

        viewModel.updateFromService(
            WorkoutTimerState(
                currentPhase = Phase(PhaseName.GET_READY, 1_000L)
            )
        )
        viewModel.onMainButtonClicked()
        advanceUntilIdle()
        assertEquals(listOf("start"), binder.calls)

        viewModel.updateFromService(WorkoutTimerState(isTimerRunning = true))
        viewModel.onMainButtonClicked()
        advanceUntilIdle()
        assertEquals(listOf("start", "pause"), binder.calls)

        viewModel.updateFromService(
            WorkoutTimerState(
                currentPhase = Phase(PhaseName.COMPLETED, 0L),
                isCompleted = true
            )
        )
        assertTrue(viewModel.uiStates.value.isCompleted)
        viewModel.onMainButtonClicked()
        advanceUntilIdle()
        assertEquals(listOf("start", "pause", "close"), binder.calls)
    }

    @Test
    fun skipAndConfettiStateFollowTheTimerState() = runTest {
        val viewModel = MainViewModel()

        assertFalse(viewModel.showSkipButton())
        assertTrue(viewModel.showCountdown())

        viewModel.updateFromService(
            WorkoutTimerState(
                currentPhase = Phase(PhaseName.HARD_PHASE, 1_000L),
                isCompleted = true
            )
        )
        assertTrue(viewModel.showSkipButton())
        assertTrue(viewModel.uiStates.value.showConfetti)
    }

    private class RecordingService : WorkoutTimerService {
        val calls = mutableListOf<String>()
        override val timerState: StateFlow<WorkoutTimerState> = MutableStateFlow(WorkoutTimerState())
        override suspend fun startWorkout(workoutId: Long) { calls += "startWorkout:$workoutId" }
        override suspend fun startTimer() { calls += "start" }
        override suspend fun pauseTimer() { calls += "pause" }
        override suspend fun skipPhase() { calls += "skip" }
        override suspend fun closeWorkout() { calls += "close" }
        override suspend fun advanceToNextPhase() { calls += "advance" }
        override suspend fun debugCompleteWorkout() { calls += "debugComplete" }
    }
}
