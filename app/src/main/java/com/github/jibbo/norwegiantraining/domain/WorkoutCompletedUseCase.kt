package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import javax.inject.Inject

data class WorkoutCompletedResult(
    val session: Session,
    val progression: ProgressionResult
)

class WorkoutCompletedUseCase @Inject constructor(
    private val getTodaySession: GetTodaySessionUseCase,
    private val sessionRepository: SessionRepository,
    private val checkProgression: ApplyProgressionUseCase
) {
    suspend operator fun invoke(workoutId: Long): WorkoutCompletedResult {
        val session = getTodaySession()
        val updated = session.copy(phasesEnded = session.phasesEnded + 1)
        sessionRepository.upsertSession(updated)
        val progression = checkProgression(workoutId)
        return WorkoutCompletedResult(session = updated, progression = progression)
    }
}
