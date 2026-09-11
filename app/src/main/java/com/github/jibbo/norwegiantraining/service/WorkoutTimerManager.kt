package com.github.jibbo.norwegiantraining.service

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton

interface WorkoutTimerManager {
    fun getWorkoutTimerState(): StateFlow<WorkoutTimerState>
}

@Module
@InstallIn(SingletonComponent::class)
interface WorkoutTimerManagerModule {
    @Binds
    @Singleton
    fun bindWorkoutTimerManager(workoutTimerStateManager: WorkoutTimerStateManager): WorkoutTimerManager
}