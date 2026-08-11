package com.github.jibbo.norwegiantraining.settings

import androidx.annotation.StringRes
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.domain.FitnessLevel
import java.util.Locale


data class UiState(
    val name: String?,
    val fitnessLevel: FitnessLevel,
    val announcePhase: Boolean,
    val announcePhaseDesc: Boolean,
    val announceCountdown: Boolean,
    val vibrationEnabled: Boolean,
    val isCrashReportingEnabled: Boolean,
    val isAnalyticsEnabled: Boolean,
    val isFreeTrial: Boolean,
    val appLanguage: String?,
    val rcSubActive: Boolean = true,
    val rcExpDate: String? = null,
    val showUpgradeButton: Boolean = false
){
    val languages = listOf(
        LanguageOption(null, R.string.default_language),
        LanguageOption("en", R.string.language_english),
        LanguageOption("de", R.string.language_german),
        LanguageOption("es", R.string.language_spanish),
        LanguageOption("fr", R.string.language_french),
        LanguageOption("it", R.string.language_italian),
        LanguageOption("zh", R.string.language_chinese),
    )
    val selectedOption = languages.first { it.code == appLanguage }
}

data class LanguageOption(val code: String?, @StringRes val labelRes: Int)