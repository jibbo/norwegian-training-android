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
    fun setUserName(name: String): Unit
    fun getUserName(): String?
    fun setTTS(enabled: Boolean)
    fun getTTS(): Boolean
}

@Singleton
class UserPreferencesSharedPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepo {

    val sp = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

    override fun setUserName(name: String) {
        sp.edit { putString("username", name) }
    }

    override fun getUserName(): String? = sp.getString("username", null)

    override fun setTTS(enabled: Boolean) {
        sp.edit { putBoolean("tts", enabled) }
    }

    override fun getTTS(): Boolean = sp.getBoolean("tts", false)

}

@Module
@InstallIn(SingletonComponent::class)
interface PrefsModule {
    @Binds
    @Singleton
    fun bindPrefs(impl: UserPreferencesSharedPrefs): UserPreferencesRepo
}
