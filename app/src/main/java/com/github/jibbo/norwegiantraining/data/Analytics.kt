package com.github.jibbo.norwegiantraining.data

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface Analytics {
    fun logScreenView(name: String, clazz: Class<*>)
    fun logChangeName()
    fun logAnnouncePhase(enabled: Boolean)
    fun logAnnounceDescriptionCurrentPhase(enabled: Boolean)
    fun logAnnounceCountdownBeforeNextPhase(enabled: Boolean)
    fun logTimerNotificationEnabled(enabled: Boolean)
    fun logCrashReporting(enabled: Boolean)
    fun enabled(enabled: Boolean)
}

class FirebaseTracker @Inject constructor(
    @ApplicationContext private val context: Context
) : Analytics {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    override fun logScreenView(name: String, clazz: Class<*>) {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, clazz.name)
            }
        )
    }

    override fun logChangeName() {
        firebaseAnalytics.logEvent("change_name", null)
    }

    override fun logAnnouncePhase(enabled: Boolean) {
        firebaseAnalytics.logEvent("announce_phase", Bundle().apply {
            putBoolean("enabled", enabled)
        })
    }

    override fun logAnnounceDescriptionCurrentPhase(enabled: Boolean) {
        firebaseAnalytics.logEvent("announce_description_current_phase", Bundle().apply {
            putBoolean("enabled", enabled)
        })
    }

    override fun logAnnounceCountdownBeforeNextPhase(enabled: Boolean) {
        firebaseAnalytics.logEvent("announce_countdown_next_phase", Bundle().apply {
            putBoolean("enabled", enabled)
        })
    }

    override fun logTimerNotificationEnabled(enabled: Boolean) {
        firebaseAnalytics.logEvent("show_timer_notification", Bundle().apply {
            putBoolean("enabled", enabled)
        })
    }

    override fun logCrashReporting(enabled: Boolean) {
        firebaseAnalytics.logEvent("crash_reporting", Bundle().apply {
            putBoolean("enabled", enabled)
        })
    }

    override fun enabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }

}

@Module
@InstallIn(SingletonComponent::class)
interface AnalyticsModule {
    @Binds
    @Singleton
    fun bindAnalytics(tracker: FirebaseTracker): Analytics
}
