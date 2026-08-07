package com.github.jibbo.norwegiantraining.util

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {
    private const val LOCALE_KEY = "app_locale"

    fun setLocale(context: Context, languageCode: String?) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(LOCALE_KEY, languageCode).apply()
        applyLocale(context)
    }

    fun getLocale(context: Context): String? {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString(LOCALE_KEY, null)
    }

    fun applyLocale(context: Context): Locale {
        val languageCode = getLocale(context)
        val locale = if (languageCode.isNullOrEmpty()) {
            Configuration(context.resources.configuration).locales.get(0)
        } else {
            when (languageCode) {
                "en" -> Locale.ENGLISH
                "de" -> Locale.GERMAN
                "es" -> Locale.forLanguageTag("es")
                "fr" -> Locale.FRENCH
                "it" -> Locale.ITALIAN
                "zh" -> Locale.CHINESE
                else -> Configuration(context.resources.configuration).locales.get(0)
            }
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
        return locale
    }
}