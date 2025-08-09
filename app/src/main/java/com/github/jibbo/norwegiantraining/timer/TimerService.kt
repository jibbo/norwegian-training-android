package com.github.jibbo.norwegiantraining.timer

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerService : Service(), SensorEventListener {

    private var currentTargetTimeMillis: Long = 0L
    private var isTimerRunning: Boolean = false
    private var remainingTimeOnPauseMillis: Long = 0L

    lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        const val TAG = "TimerService"

        const val ACTION_START = "com.github.jibbo.norwegiantraining.service.START"
        const val ACTION_PAUSE = "com.github.jibbo.norwegiantraining.service.PAUSE"
        const val ACTION_STOP =
            "com.github.jibbo.norwegiantraining.service.STOP" // Optional: if you want to explicitly stop

        const val EXTRA_TARGET_TIME_MILLIS = "targetTimeMillis"

        private val timerFlow = MutableSharedFlow<Long>()
        val timerDataFlow = timerFlow.asSharedFlow()
        private val heartRateFlow = MutableStateFlow<Float?>(null)
        val heartRateDataFlow = heartRateFlow.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        // TODO
//        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        NotificationUtils.createNotificationChannel(this)
        Log.d(TAG, "TimerService Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "onStartCommand received action: $action")

        when (action) {
            ACTION_START -> {
                currentTargetTimeMillis = intent.getLongExtra(EXTRA_TARGET_TIME_MILLIS, 0L)
                startTimerInternal()
                // TODO start heart monitor
            }

            ACTION_PAUSE -> pauseTimerInternal()

            ACTION_STOP -> stopTimerInternal()

            else -> {
                Log.w(TAG, "Unknown or null action received: $action")
                stopSelfIfIdle()
            }
        }
        return START_STICKY // Restart service if killed, and re-deliver last intent (or null if no intent)
    }

    private fun startTimerInternal() {
        if (currentTargetTimeMillis > System.currentTimeMillis()) {
            isTimerRunning = true
            NotificationUtils.showNotification(this, currentTargetTimeMillis)
            Log.d(TAG, "Timer started with target: $currentTargetTimeMillis")
        } else {
            Log.w(TAG, "Invalid target time for START action.")
            stopSelfIfIdle()
        }
    }

    private fun pauseTimerInternal() {
        isTimerRunning = false
        remainingTimeOnPauseMillis =
            if (currentTargetTimeMillis > System.currentTimeMillis()) {
                currentTargetTimeMillis - System.currentTimeMillis()
            } else {
                0L
            }
//        updateNotificationContent("Timer Paused", "Paused")
        // To allow the service to be stopped if paused and nothing is holding it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(false)
        }
        Log.d(TAG, "Timer paused")
    }

    private fun stopTimerInternal() {
        isTimerRunning = false
        stopTimerInternal()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        stopSelf()
        Log.d(TAG, "Timer stopped and service stopping")
    }

    private fun tryToStartMonitoring() {
        if (heartRateSensor == null) {
            heartRateFlow.value = null
            return
        }
        sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
        heartRateFlow.value = null
    }

    private fun stopMonitoring() {
        sensorManager.unregisterListener(this)
        heartRateFlow.value = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val hrValue = event.values[0]
            if (hrValue > 0) { // Often 0 is an invalid reading
                heartRateFlow.value = hrValue
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // TODO
        // Can be used to inform the user about sensor accuracy
        // For example, if accuracy is SENSOR_STATUS_UNRELIABLE or SENSOR_STATUS_NO_CONTACT
    }

    private fun stopSelfIfIdle() {
        if (!isTimerRunning) { // Add more conditions if needed
            Log.d(TAG, "Service is idle, stopping self.")
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        // We are not using binding in this iteration, so return null
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "TimerService Destroyed")
        stopTimerInternal()
        super.onDestroy()
    }
}
