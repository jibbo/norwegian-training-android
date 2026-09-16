package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.Workout
import javax.inject.Inject

class GetTodaySessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(workout: Workout? = null): Session {
        sessionRepository.getTodaySession()
            ?.takeUnless { it.isManual }
            ?.let { existing ->
                if (workout != null && existing.workoutId == null) {
                    val updated = existing.copy(
                        workoutId = workout.id,
                        name = workout.name,
                        duration = workout.totalTime.toLong(),
                    )
                    sessionRepository.upsertSession(updated)
                    return updated
                }
                return existing
            }
        val newSession = workout?.let {
            Session(workoutId = it.id, name = it.name, duration = it.totalTime.toLong())
        } ?: Session()
        val id = sessionRepository.insertSession(newSession)
        return newSession.copy(id = id)
    }

    suspend fun createSession(workout: Workout): Session {
        val session = Session(workoutId = workout.id, name = workout.name, duration = workout.totalTime.toLong())
        return session.copy(id = sessionRepository.insertSession(session))
    }
}
