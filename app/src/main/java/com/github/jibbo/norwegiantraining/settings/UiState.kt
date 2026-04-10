package com.github.jibbo.norwegiantraining.settings

import com.github.jibbo.norwegiantraining.domain.FitnessLevel


data class UiState(
    val name: String?,
    val fitnessLevel: FitnessLevel,
    val announcePhase: Boolean,
    val announcePhaseDesc: Boolean,
    val announceCountdown: Boolean,
    val isCrashReportingEnabled: Boolean,
    val isAnalyticsEnabled: Boolean,
    val isFreeTrial: Boolean,
    val rcSubActive: Boolean = true,
    val rcExpDate: String? = null,
    val showUpgradeButton: Boolean = false
)
