package com.github.jibbo.norwegiantraining.data

import android.content.Context
import android.telephony.TelephonyManager
import androidx.core.content.edit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


const val PREFS_KEY = "norwegian_training_prefs";

interface UserPreferencesRepo {
    fun setUserName(name: String?): Unit
    fun getUserName(): String?
    fun setAnnouncePhase(enabled: Boolean)
    fun getAnnouncePhase(): Boolean
    fun setAnnouncePhaseDesc(enabled: Boolean)
    fun getAnnouncePhaseDesc(): Boolean
    fun setAnnounceCountdown(enabled: Boolean)
    fun getAnnounceCountdown(): Boolean
    fun setAnalyticsEnabled(enabled: Boolean)
    fun getAnalyticsEnabled(): Boolean
    fun setCrashReportingEnabled(enabled: Boolean)
    fun getCrashReportingEnabled(): Boolean
}

@Singleton
class UserPreferencesSharedPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepo {

    val sp = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

    override fun setUserName(name: String?) {
        sp.edit { putString(KEY_USERNAME, name) }
    }

    override fun getUserName(): String? = sp.getString(KEY_USERNAME, null)


    override fun setAnnouncePhase(enabled: Boolean) {
        sp.edit { putBoolean(KEY_ANNOUNCE_PHASE, enabled) }
    }

    override fun getAnnouncePhase(): Boolean = sp.getBoolean(KEY_ANNOUNCE_PHASE, true)

    override fun setAnnouncePhaseDesc(enabled: Boolean) {
        sp.edit { putBoolean(KEY_ANNOUNCE_PHASE_DESC, enabled) }
    }

    override fun getAnnouncePhaseDesc(): Boolean = sp.getBoolean(KEY_ANNOUNCE_PHASE_DESC, true)

    override fun setAnnounceCountdown(enabled: Boolean) {
        sp.edit { putBoolean(KEY_ANNOUNCE_COUNTDOWN, enabled) }
    }

    override fun getAnnounceCountdown(): Boolean = sp.getBoolean(KEY_ANNOUNCE_COUNTDOWN, true)

    override fun setAnalyticsEnabled(enabled: Boolean) {
        sp.edit { putBoolean(KEY_ANALYTICS_ENABLED, enabled) }
    }

    override fun getAnalyticsEnabled() = sp.getBoolean(KEY_ANALYTICS_ENABLED, !isEuUser(context))

    override fun setCrashReportingEnabled(enabled: Boolean) {
        sp.edit { putBoolean(KEY_CRASHLYTICS_ENABLED, enabled) }
    }
    
    override fun getCrashReportingEnabled(): Boolean =
        sp.getBoolean(KEY_CRASHLYTICS_ENABLED, !isEuUser(context))

    companion object {
        const val KEY_ANNOUNCE_PHASE = "announce_phase"
        const val KEY_USERNAME = "username"
        const val KEY_ANNOUNCE_PHASE_DESC = "announce_phase_desc"
        const val KEY_ANNOUNCE_COUNTDOWN = "announce_countdown"
        const val KEY_CRASHLYTICS_ENABLED = "crashlytics_enabled"
        const val KEY_ANALYTICS_ENABLED = "analytics_enabled"

        fun isEuUser(context: Context): Boolean {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
            var country = tm?.simCountryIso
            country = country ?: Locale.getDefault().country
            val euCountries = arrayOf<String?>(
                "BE", "EL", "LT", "PT", "BG", "ES", "LU", "RO", "CZ", "FR", "HU", "SI", "DK", "HR",
                "MT", "SK", "DE", "IT", "NL", "FI", "EE", "CY", "AT", "SE", "IE", "LV", "PL", "UK",
                "CH", "NO", "IS", "LI"
            )
            return listOf(*euCountries).contains(country.uppercase(Locale.getDefault()))
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface PrefsModule {
    @Binds
    @Singleton
    fun bindPrefs(impl: UserPreferencesSharedPrefs): UserPreferencesRepo
}
