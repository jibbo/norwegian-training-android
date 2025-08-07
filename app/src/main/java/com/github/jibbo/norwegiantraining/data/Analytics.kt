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
}

@Module
@InstallIn(SingletonComponent::class)
interface AnalyticsModule {
    @Binds
    @Singleton
    fun bindAnalytics(tracker: FirebaseTracker): Analytics
}
