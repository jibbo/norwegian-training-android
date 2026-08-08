package com.github.jibbo.norwegiantraining.util

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {
    private const val PrefsName = "app_prefs"
    private const val LOCALE_KEY = "app_language"

    fun setLocale(context: Context, languageCode: String?) {
        context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
            .edit().putString(LOCALE_KEY, languageCode).commit()
    }

    fun getLocale(context: Context): String? {
        return context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
            .getString(LOCALE_KEY, null)
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