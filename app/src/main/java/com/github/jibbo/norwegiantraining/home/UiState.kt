package com.github.jibbo.norwegiantraining.home

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout

data class UiState(
    val name: String = "",
    val workouts: HashMap<Difficulty, List<Workout>>
)
