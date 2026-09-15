package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import javax.inject.Inject

class SkipPhaseUseCase @Inject constructor(
    private val getTodaySession: GetTodaySessionUseCase,
    private val sessionRepository: SessionRepository,
    private val workoutRepository: WorkoutRepository,
) {
    suspend operator fun invoke(workoutId: Long): Session {
        val session = getTodaySession(workoutRepository.getById(workoutId))
        val out = session.copy(
            skipCount = session.skipCount + 1
        )
        sessionRepository.upsertSession(out)
        return out
    }
}
