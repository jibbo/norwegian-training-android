package com.github.jibbo.norwegiantraining.home

import androidx.annotation.StringRes
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.Workout

sealed class UiState {
    object Loading : UiState()
    data class Loaded(
        val username: String?,
        val recommendedWorkoutId: Long?,
        val hasProgressed: Boolean,
        val workouts: Map<Difficulty, List<Workout>>,
        val weeklySessions: List<Session?>,
        val workoutProjection: HomeWorkoutProjection = HomeWorkoutProjection(
            recommendedBuiltIn = null,
            customWorkouts = emptyList(),
            remainingBuiltIns = emptyList(),
        )
    ) : UiState()

}

@StringRes
fun Difficulty.printableName(): Int = when (this) {
    Difficulty.BEGINNER -> R.string.workout_category_beginner
    Difficulty.INTERMEDIATE -> R.string.workout_category_intermediate
    Difficulty.EXPERT -> R.string.workout_category_expert
}
