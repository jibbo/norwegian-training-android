package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import java.time.temporal.ChronoUnit
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
        // 1. Fetch sessions from the later of (28 days ago) or (last progression date)
        val now = Calendar.getInstance()
        val twentyEightDaysAgo =
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -28) }.time
        val lastProgression = settingsRepository.getLastProgressionDate()
        val from = if (lastProgression != null && lastProgression.after(twentyEightDaysAgo))
            lastProgression
        else
            twentyEightDaysAgo
        val sessions = sessionRepository.getSessionsInRange(from, now.time)

        // 2. Count weeks in the rolling window that had at least 3 sessions
        val fromInstant = from.toInstant()
        val qualifyingWeeks = sessions
            .groupBy { session ->
                ChronoUnit.DAYS.between(fromInstant, session.date.toInstant()) / 7
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
            settingsRepository.setLastProgressionDate(now.time)
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
        settingsRepository.setLastProgressionDate(now.time)
        return ProgressionResult.LevelUp(nextLevel)
    }
}
