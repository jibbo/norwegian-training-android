package com.github.jibbo.norwegiantraining.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.github.jibbo.norwegiantraining.alarm.AlarmUtils

class TimerService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }


    // This is triggered when another android component sends an Intent to this running service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

// Do the work that the service needs to do here
        when (intent?.action) {
            Actions.START.toString() -> {
                start() // Call start() to create the notification and start in foreground
            }

            Actions.STOP.toString() -> stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    enum class Actions {
        START, STOP
    }

    private fun start() {
        val showNotification = AlarmUtils.showNotification(this, text = "Timer is running")
        // Start the service in the foreground
        startForeground(1, showNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up any resources here
    }
}
