package com.github.jibbo.norwegiantraining.main

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.components.AlarmReceiver
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.log.LogActivity
import com.github.jibbo.norwegiantraining.main.MainViewModel.UiCommands
import com.github.jibbo.norwegiantraining.settings.SettingsActivity
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val REQUEST_CODE_POST_NOTIFICATIONS = 123
    private val CHANNEL_ID = "alarm_channel"
    private val NOTIFICATION_ID = 1

    private var tts: TextToSpeech? = null

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
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

        createNotificationChannel(this)

        lifecycleScope.launch {
            mainViewModel.uiEvents.flowWithLifecycle(lifecycle).collect {
                when (it) {
                    is UiCommands.START_ALARM -> {
                        startAlarm(it.triggerTime, it.uiState)
                    }

                    is UiCommands.SHOW_NOTIFICATION -> {
                        checkNotificationPermission()
                        showNotification(it.triggerTime)
                    }

                    is UiCommands.STOP_ALARM -> {
                        cancelNotification()
                    }

                    is UiCommands.Speak -> {
                        speak(it.speakState.message)
                    }

                    is UiCommands.SHOW_SETTINGS -> {
                        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    }

                    is UiCommands.SHOW_CHARTS -> {
                        startActivity(Intent(this@MainActivity, LogActivity::class.java))
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
        showNotification(triggerTime)

        // TODO move to viewModel these ifs
        if (mainViewModel.shouldAnnouncePhase()) {
            speak(uiState.stepMessage(), TextToSpeech.QUEUE_FLUSH)
        }
        if (mainViewModel.shouldAnnouncePhaseDesc()) {
            speak(uiState.description())
        }
    }


    private fun cancelNotification() {
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
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

    private fun showNotification(triggerTime: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your notification icon
            .setContentTitle("Norwegian Training Alarm")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setWhen(triggerTime)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)

        NotificationManagerCompat
            .from(this)
            .notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Channel"
            val descriptionText = "Channel for alarm notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun speak(@StringRes textId: Int, queueMode: Int = TextToSpeech.QUEUE_ADD) {
        tts?.speak(
            getString(textId),
            queueMode, // Adds to the queue, doesn't interrupt
            null,
            "countdown_$textId" // Unique utterance ID for this specific announcement
        )
    }
}
