package com.github.jibbo.norwegiantraining.service

import android.os.Binder
import kotlinx.coroutines.flow.StateFlow

interface IWorkoutTimerService {
    val timerState: StateFlow<WorkoutTimerState>
    suspend fun startWorkout(workoutId: Long)
    suspend fun startTimer()
    suspend fun pauseTimer()
    suspend fun skipPhase()
    suspend fun closeWorkout()
}

class WorkoutServiceBinder(
    private val service: WorkoutTimerService
) : Binder(), IWorkoutTimerService {

    override val timerState: StateFlow<WorkoutTimerState>
        get() = service.timerState

    override suspend fun startWorkout(workoutId: Long) {
        service.startWorkout(workoutId)
    }

    override suspend fun startTimer() {
        service.startTimer()
    }

    override suspend fun pauseTimer() {
        service.pauseTimer()
    }

    override suspend fun skipPhase() {
        service.skipPhase()
    }

    override suspend fun closeWorkout() {
        service.closeWorkout()
    }
}
