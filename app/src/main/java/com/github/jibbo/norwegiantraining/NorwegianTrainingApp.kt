package com.github.jibbo.norwegiantraining

import android.app.Application
import com.github.jibbo.norwegiantraining.data.Analytics
import com.github.jibbo.norwegiantraining.data.UserPreferencesRepo
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NorwegianTrainingApp : Application() {
    @Inject
    lateinit var settingsRepo: UserPreferencesRepo

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate() {
        super.onCreate()
        if (settingsRepo.getAnalyticsEnabled()) {
            analytics.enable()
        } else {
            analytics.disable()
        }
    }
}
