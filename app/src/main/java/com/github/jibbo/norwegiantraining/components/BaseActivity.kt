package com.github.jibbo.norwegiantraining.components

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.github.jibbo.norwegiantraining.data.Analytics
import com.github.jibbo.norwegiantraining.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity() : ComponentActivity() {
    @Inject
    lateinit var analytics: Analytics

    override fun attachBaseContext(newBase: Context) {
        val locale = LocaleHelper.getLocale(newBase)
        val context = if (locale.isNullOrEmpty()) {
            newBase
        } else {
            val resolvedLocale = when (locale) {
                "en" -> Locale.ENGLISH
                "de" -> Locale.GERMAN
                "es" -> Locale.forLanguageTag("es")
                "fr" -> Locale.FRENCH
                "it" -> Locale.ITALIAN
                "zh" -> Locale.CHINESE
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
