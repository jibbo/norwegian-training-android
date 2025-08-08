package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import javax.inject.Inject

class SkipPhaseUseCase @Inject constructor(
    private val getTodaySession: GetTodaySessionUseCase,
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): Session {
        val session = getTodaySession()
        val out = session.copy(
            skipCount = session.skipCount + 1
        )
        sessionRepository.upsertSession(out)
        return out
    }
}
