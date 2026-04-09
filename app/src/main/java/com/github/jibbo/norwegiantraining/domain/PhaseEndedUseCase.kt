package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import javax.inject.Inject

data class PhaseEndedResult(
    val session: Session,
    val progression: ProgressionResult
)

class PhaseEndedUseCase @Inject constructor(
    private val getTodaySession: GetTodaySessionUseCase,
    private val sessionRepository: SessionRepository,
    private val checkProgression: CheckProgressionUseCase
) {
    suspend operator fun invoke(): PhaseEndedResult {
        val session = getTodaySession()
        val updated = session.copy(phasesEnded = session.phasesEnded + 1)
        sessionRepository.upsertSession(updated)
        val progression = checkProgression()
        return PhaseEndedResult(session = updated, progression = progression)
    }
}