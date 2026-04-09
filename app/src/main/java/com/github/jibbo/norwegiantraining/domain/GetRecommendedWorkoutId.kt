package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.data.Workout
import javax.inject.Inject

class GetRecommendedWorkoutId @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(workouts: Map<Difficulty, List<Workout>>): Long? {
        // If progression has already set a specific workout, validate it still exists
        settingsRepository.getRecommendedWorkoutId()?.let { id ->
            if (workouts.values.flatten().any { it.id == id }) return id
            settingsRepository.setRecommendedWorkoutId(-1L)
        }
        // Otherwise fall back to the first workout of the onboarding-selected difficulty
        val fitnessLevel = settingsRepository.getFitnessLevel()
        val difficulty = fitnessLevel.toDifficulty()
        return workouts[difficulty]?.firstOrNull()?.id
            ?: workouts.entries.firstOrNull()?.value?.firstOrNull()?.id
    }
}
