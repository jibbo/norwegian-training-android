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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> {
                start()
            }

            Actions.STOP.toString() -> stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val showNotification = AlarmUtils.showNotification(this, text = "Timer is running")
        startForeground(1, showNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    enum class Actions {
        START, STOP
    }
}
