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

    override fun getAll(): Flow<List<Workout>> = flow.asStateFlow()

    override suspend fun getByDifficulty(difficulty: Difficulty): List<Workout> =
        workouts.filter { it.difficulty == difficulty }

    override suspend fun getById(id: Long): Workout? = workouts.firstOrNull { it.id == id }

    override suspend fun getDifficulties(): List<Difficulty> =
        workouts.map { it.difficulty }.distinct()

    override suspend fun insert(vararg workouts: Workout) {
        this.workouts.addAll(workouts)
        flow.value = this.workouts.toList()
    }

    override suspend fun insert(workouts: List<Workout>) {
        this.workouts.addAll(workouts)
        flow.value = this.workouts.toList()
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
