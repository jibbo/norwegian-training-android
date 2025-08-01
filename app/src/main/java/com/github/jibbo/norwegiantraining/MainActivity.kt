package com.github.jibbo.norwegiantraining

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private val CHANNEL_ID = 666
    private val NOTIFICATION_ID = 777

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            NorwegianTrainingTheme {
                MainView(mainViewModel)
            }
        }

        lifecycleScope.launch {
            mainViewModel.uiEvents.flowWithLifecycle(lifecycle).collect{
                when(it){
                    MainViewModel.UiCommands.INITIAL -> {
                    }
                    is MainViewModel.UiCommands.START_ALARM -> scheduleAlarm(it.seconds)
                    MainViewModel.UiCommands.STOP_ALARM -> TODO()
                }
            }
        }

//        createNotificationChannel(this)
    }

    private fun scheduleAlarm(seconds: Int) {
        // Set alarm after 30 seconds
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val triggerTime = System.currentTimeMillis() + seconds

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) {
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(intent)
                }
            } else {
                alarmManager?.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

//                val notification = Notification.Builder(this, CHANNEL_ID)
//                    .setChronometerCountDown(true)
//                    .setUsesChronometer(true)
//                    .setShowWhen(true)
//                    .setWhen(triggerTime)
//
//                NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
            }
        }
    }

//    private fun createNotificationChannel(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = "Alarm Channel"
//            val descriptionText = "Channel for alarm notifications"
//            val importance = NotificationManager.IMPORTANCE_HIGH
//            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
//                description = descriptionText
//            }
//
//            val notificationManager: NotificationManager =
//                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
}
