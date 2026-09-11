package com.github.jibbo.norwegiantraining.data

import com.github.jibbo.norwegiantraining.domain.FitnessLevel
import com.github.jibbo.norwegiantraining.service.WorkoutTimerManager
import com.github.jibbo.norwegiantraining.service.WorkoutTimerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import java.util.Locale

class FakeSessionRepo : SessionRepository {
    override suspend fun getSessions(
        limit: Int,
        offset: Int
    ): List<Session> = listOf()

    override suspend fun getSessionsInRange(from: Date, to: Date): List<Session> = listOf()

    override suspend fun upsertSession(session: Session): Long = -1

    override suspend fun insertSession(session: Session): Long = -1

    override suspend fun insertSessions(sessions: List<Session>) {}

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

    override fun setAnnounceOneMinute(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnnounceOneMinute(): Boolean = false

    override fun setAnnouncePause(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnnouncePause(): Boolean = false

    override fun setVibrationEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getVibrationEnabled(): Boolean = false

    override fun setAnalyticsEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getAnalyticsEnabled(): Boolean = false

    override fun setCrashReportingEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getCrashReportingEnabled(): Boolean = false

    override fun isOnboardingCompleted(): Boolean = false

    override fun onboardingCompleted() {
        TODO("Not yet implemented")
    }

    override fun getFreeTrialEndDate(): Date = Date().apply { time += 24 * 60 * 60 * 1000 }

    override fun startFreeTrial() {
        TODO("Not yet implemented")
    }

    override fun debugOnlySetFreeTrialDate(date: Date?) {
        TODO("Not yet implemented")
    }

    override fun setRecommendedWorkoutId(id: Long) {}
    override fun clearRecommendedWorkoutId() {}
    override fun getRecommendedWorkoutId(): Long? = null
    override fun setFitnessLevel(level: FitnessLevel) {}

    override fun getFitnessLevel(): FitnessLevel = FitnessLevel.BEGINNER
    override fun setLastProgressionDate(date: Date) {}
    override fun getLastProgressionDate(): Date? = null
    override fun setLastWorkoutId(id: Long) {}
    override fun getLastWorkoutId(): Long? = null
    override fun setAppLanguage(locale: Locale?) {}
    override fun getAppLanguage(): Locale = Locale.getDefault()

    override fun setShowTodayStatsInActivitySection(enabled: Boolean) {}

    override fun getShowTodayStatsInActivitySection(): Boolean = true
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

    override fun logAnnounceOneMinute(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun logAnnouncePause(enabled: Boolean) {
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

    override fun logStartFreeTrial(endDate: Date?) {
        TODO("Not yet implemented")
    }

    override fun logRevenueCatError(name: String, message: String) {
        TODO("Not yet implemented")
    }
}

class FakeWorkoutRepo : WorkoutRepository {
    private val workouts = mutableListOf<Workout>()

    override fun getAll(): Flow<List<Workout>> =
        kotlinx.coroutines.flow.flow { emit(workouts.toList()) }

    override fun getCustomWorkouts(): Flow<List<Workout>> =
        kotlinx.coroutines.flow.flow { emit(workouts.filter { it.isCustom }.sortedByDescending { it.id }) }

    override fun getBuiltInWorkouts(): Flow<List<Workout>> =
        kotlinx.coroutines.flow.flow { emit(workouts.filterNot { it.isCustom }.sortedBy { it.id }) }

    override suspend fun getByDifficulty(difficulty: Difficulty): List<Workout> =
        workouts.filter { !it.isCustom && it.difficulty == difficulty }

    override suspend fun getById(id: Long): Workout? =
        workouts.firstOrNull { it.id == id }

    override suspend fun getDifficulties(): List<Difficulty> =
        Difficulty.entries.toList()

    override suspend fun insertCustom(workout: Workout): Long {
        workouts.add(workout.copy(isCustom = true))
        return workout.id
    }

    override suspend fun updateCustom(workout: Workout): Boolean {
        val index = workouts.indexOfFirst { it.id == workout.id && it.isCustom }
        if (index == -1) return false
        workouts[index] = workout.copy(isCustom = true)
        return true
    }

    override suspend fun deleteCustom(id: Long): Boolean =
        workouts.removeIf { it.id == id && it.isCustom }

    override suspend fun insert(vararg workouts: Workout) {
        this.workouts.addAll(workouts)
    }

    override suspend fun insert(workouts: List<Workout>) {
        this.workouts.addAll(workouts)
    }

    class FakeWorkoutTimerManager : WorkoutTimerManager {
        override fun getWorkoutTimerState(): StateFlow<WorkoutTimerState>  = MutableStateFlow(WorkoutTimerState(workoutId = 42L)).asStateFlow()
    }

}
