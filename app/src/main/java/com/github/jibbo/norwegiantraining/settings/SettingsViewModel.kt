package com.github.jibbo.norwegiantraining.settings

import com.github.jibbo.norwegiantraining.data.UserPreferencesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: UserPreferencesRepo
) {
}
