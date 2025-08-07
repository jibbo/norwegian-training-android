package com.github.jibbo.norwegiantraining.components

import androidx.activity.ComponentActivity
import com.github.jibbo.norwegiantraining.data.Analytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseActivity() : ComponentActivity() {
    @Inject
    lateinit var analytics: Analytics

    override fun onResume() {
        super.onResume()
        analytics.logScreenView(this::class.java.simpleName, this::class.java)
    }
}
