package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.Workout
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class GetTodaySessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(workout: Workout? = null): Session {
        val existing = if (workout == null) {
            sessionRepository.getTodaySession()?.takeUnless { it.isManual }
        } else {
            sessionRepository.getNormalSessionForWorkoutInRange(
                workout.id,
                startOfToday(),
                endOfToday(),
            )
        }
        existing?.let { return it }

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

    private fun startOfToday(): Date = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun endOfToday(): Date = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.time
}
