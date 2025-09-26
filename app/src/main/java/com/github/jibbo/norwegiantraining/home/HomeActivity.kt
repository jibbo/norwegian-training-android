package com.github.jibbo.norwegiantraining.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.log.LogActivity
import com.github.jibbo.norwegiantraining.main.MainActivity
import com.github.jibbo.norwegiantraining.onboarding.OnboardingActivity
import com.github.jibbo.norwegiantraining.paywall.PaywallActivity
import com.github.jibbo.norwegiantraining.settings.SettingsActivity
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                Scaffold { _ ->
                    HomeView(homeViewModel)
                }
            }
        }

        lifecycleScope.launch {
            homeViewModel.uiEvents.flowWithLifecycle(lifecycle).collect {
                when (it) {
                    UiCommands.SHOW_ONBOARDING -> showOnboarding()
                    UiCommands.SHOW_SETTINGS -> showSettings()
                    UiCommands.SHOW_CHARTS -> showCharts()
                    UiCommands.SHOW_PAYWALL -> showPaywall()
                    is UiCommands.SHOW_WORKOUT -> showWorkout(it.id)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // TODO move shared preferences to datastore so that this access can be removed
        homeViewModel.refresh()
    }

    fun showOnboarding() {
        val newIntent = Intent(this@HomeActivity, OnboardingActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(newIntent)
    }

    fun showWorkout(id: Long) {
        val newIntent = Intent(this@HomeActivity, MainActivity::class.java)
        newIntent.putExtra("id", id)
        startActivity(newIntent)
    }

    private fun showSettings() {
        startActivity(Intent(this@HomeActivity, SettingsActivity::class.java))
    }

    private fun showCharts() {
        startActivity(Intent(this@HomeActivity, LogActivity::class.java))
    }

    private fun showPaywall() {
        val newIntent = Intent(this@HomeActivity, PaywallActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(newIntent)
    }

}
