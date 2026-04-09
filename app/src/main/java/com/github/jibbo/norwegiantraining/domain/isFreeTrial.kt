package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.SettingsRepository
import java.util.Date
import javax.inject.Inject

class isFreeTrial @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Boolean = settingsRepository.getFreeTrialEndDate()?.after(Date()) == true
}
