package com.github.jibbo.norwegiantraining.domain

import android.util.Log
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import com.github.jibbo.norwegiantraining.log.SessionStatus
import com.github.jibbo.norwegiantraining.log.getStatus
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
    companion object {
        private const val TAG = "ApplyProgression"
    }
    suspend operator fun invoke(completedWorkoutId: Long, session: Session): ProgressionResult {
        val status = session.getStatus()
        val isQualifying = status != SessionStatus.BAD
        Log.d(
            TAG,
            "invoke: workoutId=$completedWorkoutId, session=$session, status=$status, isQualifying=$isQualifying"
        )

        // Time-based gradual progression
        val timeBasedResult = applyTimeBased()
        Log.d(TAG, "timeBased result: $timeBasedResult")
        return timeBasedResult
    }

    private suspend fun advanceFrom(
        completedIndex: Int,
        workoutsInDifficulty: List<com.github.jibbo.norwegiantraining.data.Workout>,
        fitnessLevel: FitnessLevel,
        now: Calendar
    ): ProgressionResult? {
        val nextInDifficulty = workoutsInDifficulty.getOrNull(completedIndex + 1)
        Log.d(
            TAG,
            "advanceFrom: completedIndex=$completedIndex, totalWorkouts=${workoutsInDifficulty.size}, nextExists=${nextInDifficulty != null}, fitnessLevel=$fitnessLevel, nextLevel=${fitnessLevel.next()}"
        )
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
            .filter { it.getStatus() != SessionStatus.BAD }

        // 2. Count weeks in the rolling window that had at least 3 qualifying sessions
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
