package com.github.jibbo.norwegiantraining.onboarding

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.home.HomeActivity
import com.github.jibbo.norwegiantraining.paywall.PaywallActivity
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnboardingActivity : BaseActivity() {
    private val viewModel: OnboardingViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.permissionResult(granted = true)
        } else {
            viewModel.permissionResult(granted = false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NorwegianTrainingTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->
                    LoadingPage(innerPadding, viewModel)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiEvents.flowWithLifecycle(lifecycle).collect {
                when (it) {
                    UiCommands.SHOW_HOME -> showActivity(HomeActivity::class.java)
                    UiCommands.SHOW_PAYWALL -> showActivity(PaywallActivity::class.java)
                    is UiCommands.AskPermission -> requestPermission(it.permission)
                }
            }
        }
    }

    private fun requestPermission(permission: String) {
        permissionLauncher.launch(permission)
    }

    fun showActivity(activityClass: Class<out Activity>) {
        val intent = Intent(
            this,
            activityClass
        )
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
