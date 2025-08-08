package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import javax.inject.Inject

class SaveTodaySession @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(session: Session) {
        sessionRepository.upsertSession(session)
    }
}
