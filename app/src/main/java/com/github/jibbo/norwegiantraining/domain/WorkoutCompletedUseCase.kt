package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import com.github.jibbo.norwegiantraining.log.SessionStatus
import javax.inject.Inject

data class WorkoutCompletedResult(
    val session: Session,
    val progression: ProgressionResult
)

class WorkoutCompletedUseCase @Inject constructor(
    private val getTodaySession: GetTodaySessionUseCase,
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val workoutRepository: WorkoutRepository,
    private val checkProgression: ApplyProgressionUseCase
) {
    suspend operator fun invoke(workoutId: Long): WorkoutCompletedResult {
        val session = getTodaySession()
        val updated = session.copy(phasesEnded = session.phasesEnded + 1)
        sessionRepository.upsertSession(updated)
        val workout = workoutRepository.getById(workoutId)
        val progression = if (workout?.isCustom == true) {
            ProgressionResult.NoChange
        } else {
            checkProgression(workoutId, updated)
        }

        // Store the last completed workout only if it wasn't BAD and the workout is built-in.
        val status = progression.sessionStatus
        if (workout?.isCustom != true && status != null && status != SessionStatus.BAD) {
            settingsRepository.setLastWorkoutId(workoutId)
        }

        return WorkoutCompletedResult(session = updated, progression = progression)
    }
}
