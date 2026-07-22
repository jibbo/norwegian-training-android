package com.github.jibbo.norwegiantraining.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jibbo.norwegiantraining.data.Analytics
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.data.SessionRepository
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.github.jibbo.norwegiantraining.domain.FitnessLevel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val analytics: Analytics,
) : ViewModel() {

    private val uiStates = MutableStateFlow(
        UiState(
            name = settingsRepository.getUserName(),
            fitnessLevel = settingsRepository.getFitnessLevel(),
            announcePhase = settingsRepository.getAnnouncePhase(),
            announcePhaseDesc = settingsRepository.getAnnouncePhaseDesc(),
            announceCountdown = settingsRepository.getAnnounceCountdown(),
            isCrashReportingEnabled = settingsRepository.getCrashReportingEnabled(),
            isAnalyticsEnabled = settingsRepository.getAnalyticsEnabled(),
            isFreeTrial = settingsRepository.getFreeTrialEndDate()?.after(Date()) == true,
            rcExpDate = settingsRepository.getFreeTrialEndDate().toLocalString()
        )
    )

    init {
        if (settingsRepository.getFreeTrialEndDate()?.after(Date()) == true) {
            uiStates.value = uiStates.value.copy(
                showUpgradeButton = true
            )
        } else if (Purchases.isConfigured) {
            Purchases.sharedInstance.getCustomerInfoWith(
                onError = {},
                onSuccess = { customerInfo ->
                    val expirationDate =
                        customerInfo.entitlements.active.values.firstOrNull()?.expirationDate
                    val showUpgradeButton = expirationDate != null
                    uiStates.value = uiStates.value.copy(
                        rcSubActive = customerInfo.entitlements.active.values.isNotEmpty(),
                        rcExpDate = expirationDate?.toLocalString(),
                        showUpgradeButton = showUpgradeButton
                    )
                }
            )
        }
    }

    val uiState = uiStates.asStateFlow()

    fun setName(name: String) {
        settingsRepository.setUserName(name)
        uiStates.value = uiStates.value.copy(name = name)
        analytics.logChangeName()
    }

    fun setAnnouncePhase(enabled: Boolean) {
        settingsRepository.setAnnouncePhase(enabled)
        uiStates.value = uiStates.value.copy(announcePhase = enabled)
        analytics.logAnnouncePhase(enabled)
    }

    fun setAnnouncePhaseDesc(enabled: Boolean) {
        settingsRepository.setAnnouncePhaseDesc(enabled)
        uiStates.value = uiStates.value.copy(announcePhaseDesc = enabled)
        analytics.logAnnounceDescriptionCurrentPhase(enabled)
    }

    fun setAnnounceCountdown(enabled: Boolean) {
        settingsRepository.setAnnounceCountdown(enabled)
        uiStates.value = uiStates.value.copy(announceCountdown = enabled)
        analytics.logAnnounceCountdownBeforeNextPhase(enabled)
    }

    fun toggleAnalytics(enabled: Boolean) {
        analytics.enabled(enabled)
        settingsRepository.setAnalyticsEnabled(enabled)
        uiStates.value = uiStates.value.copy(isAnalyticsEnabled = enabled)
    }

    fun toggleCrashReporting(enabled: Boolean) {
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enabled
        settingsRepository.setCrashReportingEnabled(enabled)
        uiStates.value = uiStates.value.copy(isCrashReportingEnabled = enabled)
        // TODO get rid of this if by having analytics check internally
        if (settingsRepository.getAnalyticsEnabled()) {
            analytics.logCrashReporting(enabled)
        }
    }

    /**
     * Debug: sets both free trial and grace period to expired dates,
     * so the user appears past trial but no longer has grace access.
     */
    fun debugOnlySetTrialAndGraceExpired() {
        settingsRepository.debugOnlySetTrialAndGraceExpired()
        uiStates.value = uiStates.value.copy(
            isFreeTrial = false,
            showUpgradeButton = true
        )
    }

    /**
     * Seeds the database with 12 sessions (3 per week x 4 weeks) and sets the
     * recommended workout to the last one in the current difficulty, so that
     * completing one more workout triggers a level-up.
     */
    fun seedLevelUpTest() {
        settingsRepository.setFitnessLevel(FitnessLevel.BEGINNER)
        settingsRepository.clearRecommendedWorkoutId()
        viewModelScope.launch {
            val todaySession = sessionRepository.getTodaySession()
            val copy = todaySession?.copy(skipCount = 0) ?: Session()
            sessionRepository.upsertSession(copy)
        }
        settingsRepository.setRecommendedWorkoutId(5L) // "Not So Beginner" (last BEGINNER)
        uiStates.value = uiStates.value.copy(fitnessLevel = FitnessLevel.BEGINNER)

        viewModelScope.launch {
            // 3 sessions per week across 4 weeks within the 28-day window
            val daysAgo = listOf(25, 26, 27, 18, 19, 20, 11, 12, 13, 4, 5, 6)
            val sessions = daysAgo.map { days ->
                val date = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -days)
                }.time
                Session(phasesEnded = 8, skipCount = 0, date = date)
            }
            sessionRepository.insertSessions(sessions)
        }
    }
}

fun Date?.toLocalString() = this?.let { SimpleDateFormat.getDateInstance().format(it) }
