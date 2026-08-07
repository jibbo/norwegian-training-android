package com.github.jibbo.norwegiantraining.components

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.github.jibbo.norwegiantraining.data.Analytics
import com.github.jibbo.norwegiantraining.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity() : ComponentActivity() {
    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        val locale = LocaleHelper.applyLocale(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun onResume() {
        super.onResume()
        analytics.logScreenView(this::class.java.simpleName, this::class.java)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val locale = LocaleHelper.applyLocale(this)
        super.onConfigurationChanged(newConfig)
    }
}
