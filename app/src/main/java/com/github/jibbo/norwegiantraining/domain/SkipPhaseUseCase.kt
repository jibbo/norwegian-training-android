package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.SessionRepository
import javax.inject.Inject

class SkipPhaseUseCase @Inject constructor(
    private val getTodaySession: GetTodaySessionUseCase,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke() {
        val session = getTodaySession()
        sessionRepository.upsertSession(
            session.copy(
                skipCount = session.skipCount + 1
            )
        )
    }
}
