package com.github.jibbo.norwegiantraining.testutils

import com.github.jibbo.norwegiantraining.data.Analytics
import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.data.WorkoutRepository
import com.github.jibbo.norwegiantraining.domain.FitnessLevel
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeSettingsRepository : SettingsRepository {
    private var userName: String? = null
    private var announcePhase = true
    private var announcePhaseDesc = true
    private var announceCountdown = true
    private var announceOneMinute = true
    private var announcePause = true
    private var vibrationEnabled = true
    private var analyticsEnabled = true
    private var crashReportingEnabled = true
    private var onboardingCompleted = false
    private var freeTrialEndDate: Date? = null
    private var recommendedWorkoutId: Long? = null
    private var fitnessLevel = FitnessLevel.BEGINNER
    private var lastProgressionDate: Date? = null
    private var lastWorkoutId: Long? = null
    private var appLanguage: Locale? = null
    private var showTodayStatsInActivitySection = true

    override fun setUserName(name: String?) {
        userName = name
    }

    override fun getUserName(): String? = userName

    override fun setAnnouncePhase(enabled: Boolean) {
        announcePhase = enabled
    }

    override fun getAnnouncePhase(): Boolean = announcePhase

    override fun setAnnouncePhaseDesc(enabled: Boolean) {
        announcePhaseDesc = enabled
    }

    override fun getAnnouncePhaseDesc(): Boolean = announcePhaseDesc

    override fun setAnnounceCountdown(enabled: Boolean) {
        announceCountdown = enabled
    }

    override fun getAnnounceCountdown(): Boolean = announceCountdown

    override fun setAnnounceOneMinute(enabled: Boolean) {
        announceOneMinute = enabled
    }

    override fun getAnnounceOneMinute(): Boolean = announceOneMinute

    override fun setAnnouncePause(enabled: Boolean) {
        announcePause = enabled
    }

    override fun getAnnouncePause(): Boolean = announcePause

    override fun setVibrationEnabled(enabled: Boolean) {
        vibrationEnabled = enabled
    }

    override fun getVibrationEnabled(): Boolean = vibrationEnabled

    override fun setAnalyticsEnabled(enabled: Boolean) {
        analyticsEnabled = enabled
    }

    override fun getAnalyticsEnabled(): Boolean = analyticsEnabled

    override fun setCrashReportingEnabled(enabled: Boolean) {
        crashReportingEnabled = enabled
    }

    override fun getCrashReportingEnabled(): Boolean = crashReportingEnabled

    override fun isOnboardingCompleted(): Boolean = onboardingCompleted

    override fun onboardingCompleted() {
        onboardingCompleted = true
    }

    override fun getFreeTrialEndDate(): Date? = freeTrialEndDate

    override fun startFreeTrial() {
        freeTrialEndDate = Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)
    }

    override fun debugOnlySetFreeTrialDate(date: Date?) {
        freeTrialEndDate = date
    }

    override fun setRecommendedWorkoutId(id: Long) {
        recommendedWorkoutId = id
    }

    override fun clearRecommendedWorkoutId() {
        recommendedWorkoutId = null
    }

    override fun getRecommendedWorkoutId(): Long? = recommendedWorkoutId

    override fun setFitnessLevel(level: FitnessLevel) {
        fitnessLevel = level
    }

    override fun getFitnessLevel(): FitnessLevel = fitnessLevel

    override fun setLastProgressionDate(date: Date) {
        lastProgressionDate = date
    }

    override fun getLastProgressionDate(): Date? = lastProgressionDate

    override fun setLastWorkoutId(id: Long) {
        lastWorkoutId = id
    }

    override fun getLastWorkoutId(): Long? = lastWorkoutId

    override fun setAppLanguage(locale: Locale?) {
        appLanguage = locale
    }

    override fun getAppLanguage(): Locale? = appLanguage

    override fun setShowTodayStatsInActivitySection(enabled: Boolean) {
        showTodayStatsInActivitySection = enabled
    }

    override fun getShowTodayStatsInActivitySection(): Boolean = showTodayStatsInActivitySection
}

class FakeSessionRepository : SessionRepository {
    private val sessions = mutableListOf<Session>()
    private val todaySession = MutableStateFlow<Session?>(null)

    override suspend fun getSessions(limit: Int, offset: Int): List<Session> =
        sessions.sortedByDescending { it.date }.drop(offset).take(limit)

    override suspend fun getSessionsInRange(from: Date, to: Date): List<Session> =
        sessions.filter { it.date >= from && it.date <= to }

    override suspend fun upsertSession(session: Session): Long {
        val existingIndex = sessions.indexOfFirst { it.id == session.id && session.id != 0L }
        if (existingIndex >= 0) {
            sessions[existingIndex] = session
        } else {
            sessions.add(session)
        }
        todaySession.value = session
        return session.id
    }

    override suspend fun insertSession(session: Session): Long {
        sessions.add(session)
        todaySession.value = session
        return session.id
    }

    override suspend fun insertSessions(sessions: List<Session>) {
        this.sessions.addAll(sessions)
    }

    override suspend fun getTodaySession(): Session? = todaySession.value
}

class FakeWorkoutRepository : WorkoutRepository {
    private val workouts = mutableListOf<Workout>()
    private val flow = MutableStateFlow<List<Workout>>(emptyList())

    var failure: Throwable? = null

    override fun getAll(): Flow<List<Workout>> = flow.asStateFlow()

    override fun getCustomWorkouts(): Flow<List<Workout>> =
        flow.map { values -> values.filter { it.isCustom }.sortedByDescending { it.id } }

    override fun getBuiltInWorkouts(): Flow<List<Workout>> =
        flow.map { values -> values.filterNot { it.isCustom }.sortedBy { it.id } }

    override suspend fun getByDifficulty(difficulty: Difficulty): List<Workout> {
        maybeFail()
        return workouts.filter { it.difficulty == difficulty }
    }

    override suspend fun getById(id: Long): Workout? {
        maybeFail()
        return workouts.firstOrNull { it.id == id }
    }

    override suspend fun getDifficulties(): List<Difficulty> {
        maybeFail()
        return workouts.map { it.difficulty }.distinct()
    }

    override suspend fun insertCustom(workout: Workout): Long {
        maybeFail()
        val stored = workout.copy(id = workout.id.takeIf { it != 0L } ?: nextId(), isCustom = true)
        workouts.add(stored)
        publish()
        return stored.id
    }

    override suspend fun updateCustom(workout: Workout): Boolean {
        maybeFail()
        val index = workouts.indexOfFirst { it.id == workout.id && it.isCustom }
        if (index == -1) return false
        workouts[index] = workout.copy(isCustom = true)
        publish()
        return true
    }

    override suspend fun deleteCustom(id: Long): Boolean {
        maybeFail()
        val deleted = workouts.removeIf { it.id == id && it.isCustom }
        publish()
        return deleted
    }

    override suspend fun insert(vararg workouts: Workout) {
        maybeFail()
        this.workouts.addAll(workouts)
        publish()
    }

    override suspend fun insert(workouts: List<Workout>) {
        maybeFail()
        this.workouts.addAll(workouts)
        publish()
    }

    private fun nextId(): Long = (workouts.maxOfOrNull { it.id } ?: 0L) + 1L

    private fun publish() {
        flow.value = workouts.toList()
    }

    private fun maybeFail() {
        failure?.let { throw it }
    }
}

class FakeAnalytics : Analytics {
    val calls = mutableListOf<String>()

    override fun logScreenView(name: String, clazz: Class<*>) {
        calls += "screen:$name"
    }

    override fun logChangeName() {
        calls += "change_name"
    }

    override fun logAnnouncePhase(enabled: Boolean) {
        calls += "announce_phase:$enabled"
    }

    override fun logAnnounceDescriptionCurrentPhase(enabled: Boolean) {
        calls += "announce_phase_desc:$enabled"
    }

    override fun logAnnounceCountdownBeforeNextPhase(enabled: Boolean) {
        calls += "announce_countdown:$enabled"
    }

    override fun logAnnounceOneMinute(enabled: Boolean) {
        calls += "announce_one_minute:$enabled"
    }

    override fun logAnnouncePause(enabled: Boolean) {
        calls += "announcePause:$enabled"
    }

    override fun logTimerNotificationEnabled(enabled: Boolean) {
        calls += "timer_notification:$enabled"
    }

    override fun logCrashReporting(enabled: Boolean) {
        calls += "crash_reporting:$enabled"
    }

    override fun enabled(enabled: Boolean) {
        calls += "analytics:$enabled"
    }

    override fun logStartFreeTrial(endDate: Date?) {
        calls += "free_trial"
    }

    override fun logRevenueCatError(name: String, message: String) {
        calls += "revenuecat_error:$name"
    }
}
