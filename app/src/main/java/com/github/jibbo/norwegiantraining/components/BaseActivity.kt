package com.github.jibbo.norwegiantraining.components

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.github.jibbo.norwegiantraining.data.Analytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity() : ComponentActivity() {
    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        enableEdgeToEdge()
    }

    override fun onResume() {
        super.onResume()
        analytics.logScreenView(this::class.java.simpleName, this::class.java)
    }
}
