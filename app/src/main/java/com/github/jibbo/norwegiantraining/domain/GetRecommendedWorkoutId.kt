package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.FitnessLevel
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.data.Workout
import javax.inject.Inject

class GetRecommendedWorkoutId @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(workouts: Map<Difficulty, List<Workout>>): Long {
        // If progression has already set a specific workout, use that
        settingsRepository.getRecommendedWorkoutId()?.let { return it }
        // Otherwise fall back to the first workout of the onboarding-selected difficulty
        val fitnessLevel = settingsRepository.getFitnessLevel()
        val difficulty = when (fitnessLevel) {
            FitnessLevel.BEGINNER -> Difficulty.BEGINNER
            FitnessLevel.OCCASIONAL -> Difficulty.INTERMEDIATE
            FitnessLevel.FIT -> Difficulty.EXPERT
        }
        return workouts[difficulty]?.firstOrNull()?.id
            ?: workouts.entries.firstOrNull()?.value?.firstOrNull()?.id ?: 0
    }
}
