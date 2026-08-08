package com.github.jibbo.norwegiantraining.components

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.github.jibbo.norwegiantraining.data.Analytics
import android.content.SharedPreferences
import com.github.jibbo.norwegiantraining.data.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity() : ComponentActivity() {
    @Inject
    lateinit var analytics: Analytics

    override fun attachBaseContext(newBase: Context) {
        val prefs: SharedPreferences = newBase.getSharedPreferences(SettingsRepository.PREFS_KEY, Context.MODE_PRIVATE)
        val locale = prefs.getString(SettingsRepository.KEY_APP_LANGUAGE, null)
        val context = if (locale.isNullOrEmpty()) {
            newBase
        } else {
            val resolvedLocale = when (locale) {
                "en" -> Locale.forLanguageTag("en")
                "de" -> Locale.forLanguageTag("de")
                "es" -> Locale.forLanguageTag("es")
                "fr" -> Locale.forLanguageTag("fr")
                "it" -> Locale.forLanguageTag("it")
                "zh" -> Locale.forLanguageTag("zh")
                else -> Configuration(newBase.resources.configuration).locales.get(0)
            }
            Locale.setDefault(resolvedLocale)
            val config = Configuration(newBase.resources.configuration)
            config.setLocale(resolvedLocale)
            newBase.createConfigurationContext(config)
        }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun onResume() {
        super.onResume()
        analytics.logScreenView(this::class.java.simpleName, this::class.java)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}
