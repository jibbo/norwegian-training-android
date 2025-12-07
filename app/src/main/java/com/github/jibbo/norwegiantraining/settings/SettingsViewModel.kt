package com.github.jibbo.norwegiantraining.settings

import androidx.lifecycle.ViewModel
import com.github.jibbo.norwegiantraining.data.Analytics
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val analytics: Analytics,
) : ViewModel() {

    private val uiStates = MutableStateFlow(
        UiState(
            name = settingsRepository.getUserName(),
            announcePhase = settingsRepository.getAnnouncePhase(),
            announcePhaseDesc = settingsRepository.getAnnouncePhaseDesc(),
            announceCountdown = settingsRepository.getAnnounceCountdown(),
            isCrashReportingEnabled = settingsRepository.getCrashReportingEnabled(),
            isAnalyticsEnabled = settingsRepository.getAnalyticsEnabled()
        )
    )
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
}
