package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.SettingsRepository
import java.util.Date
import javax.inject.Inject

class IsGracePeriodExpired @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Boolean {
        val endDate = settingsRepository.getGracePeriodEndDate()
        return endDate == null || Date().after(endDate)
    }
}
