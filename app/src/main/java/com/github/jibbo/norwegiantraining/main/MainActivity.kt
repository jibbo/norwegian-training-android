package com.github.jibbo.norwegiantraining.main

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.jibbo.norwegiantraining.alarm.AlarmReceiver
import com.github.jibbo.norwegiantraining.alarm.AlarmUtils
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.log.LogActivity
import com.github.jibbo.norwegiantraining.main.MainViewModel.UiCommands
import com.github.jibbo.norwegiantraining.onboarding.OnboardingActivity
import com.github.jibbo.norwegiantraining.paywall.PaywallActivity
import com.github.jibbo.norwegiantraining.settings.SettingsActivity
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val REQUEST_CODE_POST_NOTIFICATIONS = 123

    private var tts: TextToSpeech? = null

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                MainView(
                    mainViewModel = mainViewModel, // Add a callback for when timer finishes in composable
                )
            }
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            } else {
                Log.e("tts", "not working")
                tts = null
            }
        }

        AlarmUtils.createNotificationChannel(this)

        lifecycleScope.launch {
            mainViewModel.uiEvents.flowWithLifecycle(lifecycle).collect {
                when (it) {
                    is UiCommands.SHOW_ONBOARDING -> {
                        val newIntent = Intent(this@MainActivity, OnboardingActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(newIntent)
                    }

                    is UiCommands.START_ALARM -> {
                        startAlarm(it.triggerTime, it.uiState)
                    }

                    is UiCommands.SHOW_NOTIFICATION -> {
                        checkNotificationPermission()
                        AlarmUtils.showNotification(this@MainActivity, it.triggerTime)
                    }

                    is UiCommands.PAUSE_ALARM -> {
                        AlarmUtils.dismissNotification(this@MainActivity)
                    }

                    is UiCommands.Speak -> {
                        speak(it.speakState.message, it.flush)
                    }

                    is UiCommands.SHOW_SETTINGS -> {
                        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    }

                    is UiCommands.SHOW_CHARTS -> {
                        startActivity(Intent(this@MainActivity, LogActivity::class.java))
                    }

                    is UiCommands.SHOW_PAYWALL -> {
                        val newIntent = Intent(this@MainActivity, PaywallActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(newIntent)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // TODO move shared preferences to datastore so that this access can be removed
        mainViewModel.refresh()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                mainViewModel.permissionGranted()
            } else {
                // Permission denied. Handle appropriately
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
    }

    private fun startAlarm(triggerTime: Long, uiState: UiState) {
        scheduleAlarm(triggerTime)
        checkNotificationPermission()
        AlarmUtils.showNotification(this, triggerTime)
    }

    private fun scheduleAlarm(triggerTime: Long) {
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) {
                Intent().also {
                    it.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(it)
                }
            } else {
                alarmManager?.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun speak(@StringRes textId: Int, flush: Boolean = false) {
        tts?.speak(
            getString(textId),
            if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
            null,
            "countdown_$textId"
        )
    }
}
