package com.github.jibbo.norwegiantraining

import android.app.Application
import com.github.jibbo.norwegiantraining.data.Analytics
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NorwegianTrainingApp : Application() {
    @Inject
    lateinit var settingsRepo: SettingsRepository

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate() {
        super.onCreate()
        analytics.enabled(settingsRepo.getAnalyticsEnabled())
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled =
            settingsRepo.getCrashReportingEnabled()
    }
}
