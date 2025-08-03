package com.github.jibbo.norwegiantraining.data

import android.content.Context
import androidx.core.content.edit
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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

    override fun getAnnouncePhase(): Boolean = sp.getBoolean(KEY_ANNOUNCE_PHASE, false)

    override fun setAnnouncePhaseDesc(enabled: Boolean) {
        sp.edit { putBoolean(KEY_ANNOUNCE_PHASE_DESC, enabled) }
    }

    override fun getAnnouncePhaseDesc(): Boolean = sp.getBoolean(KEY_ANNOUNCE_PHASE_DESC, false)

    override fun setAnnounceCountdown(enabled: Boolean) {
        sp.edit { putBoolean(KEY_ANNOUNCE_COUNTDOWN, enabled) }
    }

    override fun getAnnounceCountdown(): Boolean = sp.getBoolean(KEY_ANNOUNCE_COUNTDOWN, false)

    companion object {
        const val KEY_ANNOUNCE_PHASE = "announce_phase"
        const val KEY_USERNAME = "username"
        const val KEY_ANNOUNCE_PHASE_DESC = "announce_phase_desc"
        const val KEY_ANNOUNCE_COUNTDOWN = "announce_countdown"
    }

}

@Module
@InstallIn(SingletonComponent::class)
interface PrefsModule {
    @Binds
    @Singleton
    fun bindPrefs(impl: UserPreferencesSharedPrefs): UserPreferencesRepo
}
