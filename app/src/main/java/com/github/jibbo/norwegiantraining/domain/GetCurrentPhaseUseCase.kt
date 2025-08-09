package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.SessionRepository
import javax.inject.Inject

class GetCurrentPhaseUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): Phase {
        val todaySession = sessionRepository.getTodaySession()
        val endedPhases = todaySession?.phasesEnded ?: 0
        val skippedPhases = todaySession?.skipCount ?: 0
        val stepCount = (endedPhases + skippedPhases) % 12
        return if (endedPhases == skippedPhases && stepCount == 0) {
            Phase.GET_READY
        } else {
            Phase.fromNumber(
                stepCount
            )
        }
    }
}
