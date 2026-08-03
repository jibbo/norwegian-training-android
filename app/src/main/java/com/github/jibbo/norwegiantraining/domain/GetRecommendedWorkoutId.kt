package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.domain.FitnessLevel
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
            settingsRepository.clearRecommendedWorkoutId()
        }
        // Otherwise fall back to the first workout of the onboarding-selected difficulty
        val fitnessLevel = settingsRepository.getFitnessLevel()
        val difficulty = fitnessLevel.toDifficulty()
        val fallbackId = workouts[difficulty]?.firstOrNull()?.id
            ?: workouts.entries.firstOrNull()?.value?.firstOrNull()?.id

        // Override with last completed workout if it's "higher" (non-BAD)
        settingsRepository.getLastWorkoutId()?.let { lastWorkoutId ->
            val lastWorkout = workouts.values.flatten().find { it.id == lastWorkoutId }
            if (lastWorkout != null) {
                val recommendedId = fallbackId
                if (isHigherWorkout(lastWorkout, recommendedId, workouts)) {
                    return lastWorkout.id
                }
            }
        }

        return fallbackId
    }

    private fun isHigherWorkout(
        lastWorkout: Workout,
        recommendedId: Long?,
        workouts: Map<Difficulty, List<Workout>>
    ): Boolean {
        val recommendedWorkout = recommendedId?.let { id ->
            workouts.values.flatten().find { it.id == id }
        }

        // If no workout is recommended yet (first launch), the last workout is "higher"
        if (recommendedId == null) return true

        // Same difficulty: check if last workout has a higher index
        if (lastWorkout.difficulty == recommendedWorkout?.difficulty) {
            val difficultyWorkouts = workouts[lastWorkout.difficulty]
                ?: return false
            val lastIdx = difficultyWorkouts.indexOfFirst { it.id == lastWorkout.id }
            val recIdx = difficultyWorkouts.indexOfFirst { it.id == recommendedWorkout.id }
            return lastIdx > recIdx
        }

        // Different difficulty: check if last workout is at a higher level
        val lastLevel = FitnessLevel.fromDifficulty(lastWorkout.difficulty)
        val recLevel = recommendedWorkout?.let { FitnessLevel.fromDifficulty(it.difficulty) }
            ?: return false
        return lastLevel > recLevel
    }

    fun hasProgressed(): Boolean = (settingsRepository.getRecommendedWorkoutId() ?: 0L) > 0
}
