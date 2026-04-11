package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class ProgressionResult {
    object NoChange : ProgressionResult()
    data class NextWorkout(val workoutId: Long) : ProgressionResult()
    data class LevelUp(val newLevel: FitnessLevel) : ProgressionResult()
}

class ApplyProgressionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(completedWorkoutId: Long): ProgressionResult {
        // Path 1: Skip-ahead — if the user completed a workout ahead of their
        // recommendation, move the pointer forward immediately.
        val skipAheadResult = applySkipAhead(completedWorkoutId)
        if (skipAheadResult != null) return skipAheadResult

        // Path 2: Time-based gradual progression (existing logic).
        return applyTimeBased()
    }

    private suspend fun applySkipAhead(completedWorkoutId: Long): ProgressionResult? {
        val completedWorkout = workoutRepository.getById(completedWorkoutId) ?: return null
        val now = Calendar.getInstance()

        val currentFitnessLevel = settingsRepository.getFitnessLevel()
        val currentDifficulty = currentFitnessLevel.toDifficulty()

        val completedDifficulty = completedWorkout.difficulty

        // User went back to a lower difficulty — respect that choice
        if (completedDifficulty.ordinal < currentDifficulty.ordinal) {
            val newLevel = FitnessLevel.fromDifficulty(completedDifficulty)
            val workoutsInDifficulty = workoutRepository
                .getByDifficulty(completedDifficulty)
                .sortedBy { it.id }
            val completedIndex = workoutsInDifficulty.indexOfFirst { it.id == completedWorkoutId }
            if (completedIndex == -1) return null

            settingsRepository.setFitnessLevel(newLevel)
            return advanceFrom(completedIndex, workoutsInDifficulty, newLevel, now)
                ?: ProgressionResult.NextWorkout(completedWorkoutId)
        }

        if (completedDifficulty == currentDifficulty) {
            // Same difficulty: check if the completed workout is ahead of recommendation
            val workoutsInDifficulty = workoutRepository
                .getByDifficulty(currentDifficulty)
                .sortedBy { it.id }

            val currentRecommendedId = settingsRepository.getRecommendedWorkoutId()
                ?: workoutsInDifficulty.firstOrNull()?.id
                ?: return null

            val recommendedIndex =
                workoutsInDifficulty.indexOfFirst { it.id == currentRecommendedId }
            val completedIndex = workoutsInDifficulty.indexOfFirst { it.id == completedWorkoutId }
            if (recommendedIndex == -1 || completedIndex == -1) return null

            // Not ahead — let time-based logic handle it
            if (completedIndex <= recommendedIndex) return null

            return advanceFrom(completedIndex, workoutsInDifficulty, currentFitnessLevel, now)
        }

        // Higher difficulty: user jumped ahead across difficulties
        val newLevel = FitnessLevel.fromDifficulty(completedDifficulty)
        val workoutsInDifficulty = workoutRepository
            .getByDifficulty(completedDifficulty)
            .sortedBy { it.id }
        val completedIndex = workoutsInDifficulty.indexOfFirst { it.id == completedWorkoutId }
        if (completedIndex == -1) return null

        settingsRepository.setFitnessLevel(newLevel)
        return advanceFrom(completedIndex, workoutsInDifficulty, newLevel, now)
            ?: ProgressionResult.LevelUp(newLevel)
    }

    private suspend fun advanceFrom(
        completedIndex: Int,
        workoutsInDifficulty: List<com.github.jibbo.norwegiantraining.data.Workout>,
        fitnessLevel: FitnessLevel,
        now: Calendar
    ): ProgressionResult? {
        val nextInDifficulty = workoutsInDifficulty.getOrNull(completedIndex + 1)
        if (nextInDifficulty != null) {
            settingsRepository.setRecommendedWorkoutId(nextInDifficulty.id)
            settingsRepository.setLastProgressionDate(now.time)
            return ProgressionResult.NextWorkout(nextInDifficulty.id)
        }

        // Last workout in difficulty — level up if possible
        val nextLevel = fitnessLevel.next() ?: return null
        val nextDifficultyWorkouts = workoutRepository
            .getByDifficulty(nextLevel.toDifficulty())
            .sortedBy { it.id }
        val firstOfNextLevel = nextDifficultyWorkouts.firstOrNull() ?: return null

        settingsRepository.setFitnessLevel(nextLevel)
        settingsRepository.setRecommendedWorkoutId(firstOfNextLevel.id)
        settingsRepository.setLastProgressionDate(now.time)
        return ProgressionResult.LevelUp(nextLevel)
    }

    private suspend fun applyTimeBased(): ProgressionResult {
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
        val fromMillis = from.time
        val qualifyingWeeks = sessions
            .groupBy { session ->
                TimeUnit.MILLISECONDS.toDays(session.date.time - fromMillis) / 7
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
        if (currentIndex == -1) return ProgressionResult.NoChange

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
