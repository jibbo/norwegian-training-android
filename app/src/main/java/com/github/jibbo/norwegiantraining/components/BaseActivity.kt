package com.github.jibbo.norwegiantraining.components

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.github.jibbo.norwegiantraining.data.Analytics
import com.github.jibbo.norwegiantraining.data.SharedPreferencesSettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity : ComponentActivity() {
    @Inject
    lateinit var analytics: Analytics

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(localizeContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun onResume() {
        super.onResume()
        analytics.logScreenView(this::class.java.simpleName, this::class.java)
    }

    private fun localizeContext(newBase: Context): Context? {
        val locale = SharedPreferencesSettingsRepository(newBase).getAppLanguage()
        val context = if(locale == null){
            newBase
        }else {
            Locale.setDefault(locale)
            val config = Configuration(newBase.resources.configuration)
            config.setLocale(locale)
            newBase.createConfigurationContext(config)
        }
        return context
    }
}
