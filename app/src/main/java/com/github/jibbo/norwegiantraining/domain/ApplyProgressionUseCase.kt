package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import com.github.jibbo.norwegiantraining.log.SessionStatus
import com.github.jibbo.norwegiantraining.log.getStatus
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class ProgressionResult(val sessionStatus: SessionStatus? = null) {
    object NoChange : ProgressionResult()
    data class NextWorkout(val workoutId: Long, val status: SessionStatus? = null) : ProgressionResult(status)
    data class LevelUp(val newLevel: FitnessLevel, val status: SessionStatus? = null) : ProgressionResult(status)
}

class ApplyProgressionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(completedWorkoutId: Long, session: Session): ProgressionResult {
        val status = session.getStatus()
        return applyTimeBased(status)
    }

    private suspend fun advanceFrom(
        completedIndex: Int,
        workoutsInDifficulty: List<com.github.jibbo.norwegiantraining.data.Workout>,
        fitnessLevel: FitnessLevel,
        now: Calendar
    ): ProgressionResult? {
        val nextInDifficulty = workoutsInDifficulty.getOrNull(completedIndex + 1)
        if (nextInDifficulty != null) {
            settingsRepository.setFitnessLevel(fitnessLevel)
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

    private suspend fun applyTimeBased(status: SessionStatus): ProgressionResult {
        val now = Calendar.getInstance()
        val twentyEightDaysAgo =
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -28) }.time
        val lastProgression = settingsRepository.getLastProgressionDate()
        val from = if (lastProgression != null && lastProgression.after(twentyEightDaysAgo))
            lastProgression
        else
            twentyEightDaysAgo
        val sessions = sessionRepository.getSessionsInRange(from, now.time)
            .filter { it.getStatus() != SessionStatus.BAD }

        val fromMillis = from.time
        val qualifyingWeeks = sessions
            .groupBy { session ->
                TimeUnit.MILLISECONDS.toDays(session.date.time - fromMillis) / 7
            }
            .count { (_, weekSessions) -> weekSessions.size >= 3 }

        if (qualifyingWeeks < 4) return ProgressionResult.NoChange

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

        val nextInDifficulty = workoutsInDifficulty.getOrNull(currentIndex + 1)
        if (nextInDifficulty != null) {
            settingsRepository.setRecommendedWorkoutId(nextInDifficulty.id)
            settingsRepository.setLastProgressionDate(now.time)
            return ProgressionResult.NextWorkout(nextInDifficulty.id, status)
        }

        val nextLevel = currentFitnessLevel.next() ?: return ProgressionResult.NoChange
        val nextDifficultyWorkouts = workoutRepository
            .getByDifficulty(nextLevel.toDifficulty())
            .sortedBy { it.id }
        val firstOfNextLevel = nextDifficultyWorkouts.firstOrNull()
            ?: return ProgressionResult.NoChange

        settingsRepository.setFitnessLevel(nextLevel)
        settingsRepository.setRecommendedWorkoutId(firstOfNextLevel.id)
        settingsRepository.setLastProgressionDate(now.time)
        return ProgressionResult.LevelUp(nextLevel, status)
    }
}
