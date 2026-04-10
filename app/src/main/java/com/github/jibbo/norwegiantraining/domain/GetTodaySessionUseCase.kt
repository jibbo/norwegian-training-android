package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import javax.inject.Inject

class GetTodaySessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): Session {
        sessionRepository.getTodaySession()?.let { return it }
        val newSession = Session()
        val id = sessionRepository.insertSession(newSession)
        return newSession.copy(id = id)
    }
}
