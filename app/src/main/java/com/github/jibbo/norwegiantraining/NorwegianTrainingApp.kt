package com.github.jibbo.norwegiantraining

import android.app.Application
import com.github.jibbo.norwegiantraining.data.Analytics
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
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

        // Analytics
        analytics.enabled(!BuildConfig.DEBUG && settingsRepo.getAnalyticsEnabled())

        // Crash reports
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled =
            !BuildConfig.DEBUG && settingsRepo.getCrashReportingEnabled()

        // RevenueCat (In-app Purchases)
        Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.INFO
        Purchases.configure(
            PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_API_KEY).build()
        )
    }
}
