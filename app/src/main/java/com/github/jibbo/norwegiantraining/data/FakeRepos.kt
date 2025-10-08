package com.github.jibbo.norwegiantraining.data

import com.github.jibbo.norwegiantraining.domain.GetAllWorkouts

class FakeSessionRepo : SessionRepository {
    override suspend fun getSessions(
        limit: Int,
        offset: Int
    ): List<Session> = listOf()

    override suspend fun upsertSession(session: Session): Long = -1

    override suspend fun getTodaySession(): Session? = null
}

class FakeSettingsRepository : SettingsRepository {
    override fun setUserName(name: String?) {
        TODO("Not yet implemented")
    }

    override fun getUserName(): String? = "Didi 🖤"

    override fun setAnnouncePhase(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnnouncePhase(): Boolean = false

    override fun setAnnouncePhaseDesc(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnnouncePhaseDesc(): Boolean = false

    override fun setAnnounceCountdown(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnnounceCountdown(): Boolean = false

    override fun setAnalyticsEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnalyticsEnabled(): Boolean = false

    override fun setCrashReportingEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getCrashReportingEnabled(): Boolean = false

    override fun setShowTimerNotification(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getShowTimerNotification(): Boolean = false

    override fun isOnboardingCompleted(): Boolean = false

    override fun onboardingCompleted() {
        TODO("Not yet implemented")
    }
}

class FakeTracker : Analytics {
    override fun logScreenView(name: String, clazz: Class<*>) {
        TODO("Not yet implemented")
    }

    override fun logChangeName() {
        TODO("Not yet implemented")
    }

    override fun logAnnouncePhase(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun logAnnounceDescriptionCurrentPhase(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun logAnnounceCountdownBeforeNextPhase(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun logTimerNotificationEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun logCrashReporting(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun enabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

}

class FakeWorkoutRepo : WorkoutRepository {
    override suspend fun getAll(): List<Workout> = GetAllWorkouts.basicWorkouts.flatMap { it.value }

    override suspend fun getByDifficulty(difficulty: Difficulty): List<Workout> =
        GetAllWorkouts.basicWorkouts[difficulty].orEmpty()

    override suspend fun getById(id: Long): Workout? {
        TODO("Not yet implemented")
    }

    override suspend fun getDifficulties(): List<Difficulty> =
        GetAllWorkouts.basicWorkouts.keys.toList()

    override suspend fun insert(vararg workouts: Workout) {
        TODO("Not yet implemented")
    }

    override suspend fun insert(workouts: List<Workout>) {
        TODO("Not yet implemented")
    }

}
