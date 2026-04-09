package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.FitnessLevel
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import java.util.Calendar
import javax.inject.Inject

sealed class ProgressionResult {
    object NoChange : ProgressionResult()
    data class NextWorkout(val workoutId: Long) : ProgressionResult()
    data class LevelUp(val newLevel: FitnessLevel) : ProgressionResult()
}

class CheckProgressionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(): ProgressionResult {
        // 1. Fetch sessions from the last 28 days
        val now = Calendar.getInstance()
        val from = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -28) }.time
        val sessions = sessionRepository.getSessionsInRange(from, now.time)

        // 2. Count weeks in the rolling window that had at least 3 sessions
        val qualifyingWeeks = sessions
            .groupBy { session ->
                Calendar.getInstance().apply { time = session.date }
                    .get(Calendar.WEEK_OF_YEAR)
            }
            .count { (_, weekSessions) -> weekSessions.size >= 3 }

        // 3. Not enough qualifying weeks yet
        if (qualifyingWeeks < 4) return ProgressionResult.NoChange

        // 4. Find the current recommended workout within the current difficulty
        val currentFitnessLevel = settingsRepository.getFitnessLevel()
        val currentDifficulty = currentFitnessLevel.toDifficulty()
        val workoutsInDifficulty = workoutRepository
            .getByDifficulty(currentDifficulty)
            .sortedBy { it.id }

        val currentRecommendedId = settingsRepository.getRecommendedWorkoutId()
            ?: workoutsInDifficulty.firstOrNull()?.id
            ?: return ProgressionResult.NoChange

        val currentIndex = workoutsInDifficulty.indexOfFirst { it.id == currentRecommendedId }

        // 5. Advance to the next workout within the same difficulty if possible
        val nextInDifficulty = workoutsInDifficulty.getOrNull(currentIndex + 1)
        if (nextInDifficulty != null) {
            settingsRepository.setRecommendedWorkoutId(nextInDifficulty.id)
            return ProgressionResult.NextWorkout(nextInDifficulty.id)
        }

        // 6. Already on the last workout — level up if possible
        val nextLevel = currentFitnessLevel.next() ?: return ProgressionResult.NoChange
        val nextDifficultyWorkouts = workoutRepository
            .getByDifficulty(nextLevel.toDifficulty())
            .sortedBy { it.id }
        val firstOfNextLevel = nextDifficultyWorkouts.firstOrNull()
            ?: return ProgressionResult.NoChange

        settingsRepository.setFitnessLevel(nextLevel)
        settingsRepository.setRecommendedWorkoutId(firstOfNextLevel.id)
        return ProgressionResult.LevelUp(nextLevel)
    }

    private fun FitnessLevel.toDifficulty() = when (this) {
        FitnessLevel.BEGINNER -> Difficulty.BEGINNER
        FitnessLevel.OCCASIONAL -> Difficulty.INTERMEDIATE
        FitnessLevel.FIT -> Difficulty.EXPERT
    }

    private fun FitnessLevel.next() = when (this) {
        FitnessLevel.BEGINNER -> FitnessLevel.OCCASIONAL
        FitnessLevel.OCCASIONAL -> FitnessLevel.FIT
        FitnessLevel.FIT -> null // Already at the top
    }
}