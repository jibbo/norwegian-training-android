package com.github.jibbo.norwegiantraining

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

const val PREFS_KEY = "norwegian_training_prefs";

interface UserPreferencesRepo {
    fun setUserName(name: String): Unit
    fun getUserName(): String?
    fun setTTS(enabled: Boolean)
    fun getTTS(): Boolean
}

object UserPreferencesDataStore : UserPreferencesRepo {

    lateinit var sharedPreferences: SharedPreferences

    fun create(context: Context): UserPreferencesRepo {
        sharedPreferences = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return this
    }

    override fun setUserName(name: String) {
        sharedPreferences.edit { putString("username", name) }
    }

    override fun getUserName(): String? = sharedPreferences.getString("username", null)

    override fun setTTS(enabled: Boolean) {
        sharedPreferences.edit { putBoolean("tts", enabled) }
    }

    override fun getTTS(): Boolean = sharedPreferences.getBoolean("tts", false)

}
