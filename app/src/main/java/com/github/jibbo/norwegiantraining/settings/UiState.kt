package com.github.jibbo.norwegiantraining.settings


data class UiState(
    val name: String?,
    val announcePhase: Boolean,
    val announcePhaseDesc: Boolean,
    val announceCountdown: Boolean,
    val isCrashReportingEnabled: Boolean,
    val isAnalyticsEnabled: Boolean,
    val isTimerNotificationEnabled: Boolean,
)
