package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.SettingsRepository
import javax.inject.Inject

class IsOnboardingCompleted @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Boolean = settingsRepository.isOnboardingCompleted()
}
