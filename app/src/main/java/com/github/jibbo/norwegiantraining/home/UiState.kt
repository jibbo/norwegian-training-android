package com.github.jibbo.norwegiantraining.home

import androidx.annotation.StringRes
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout

sealed class UiState {
    object Loading : UiState()
    data class Loaded(
        val username: String?,
        val workouts: Map<Difficulty, List<Workout>>
    ) : UiState()
}

@StringRes
fun Difficulty.printableName(): Int = when (this) {
    Difficulty.BEGINNER -> R.string.workout_category_beginner
    Difficulty.INTERMEDIATE -> R.string.workout_category_intermediate
    Difficulty.EXPERT -> R.string.workout_category_expert
}
