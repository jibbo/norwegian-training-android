package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import javax.inject.Inject

class GetTodaySessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): Session {
        val session = sessionRepository.getTodaySession() ?: Session()
        val id = sessionRepository.upsertSession(session)
        if (id > 0) {
            return session.copy(id = id)
        }
        return session
    }
}
