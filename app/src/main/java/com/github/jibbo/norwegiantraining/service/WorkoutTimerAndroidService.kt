package com.github.jibbo.norwegiantraining.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.alarm.AlarmReceiver
import com.github.jibbo.norwegiantraining.domain.PhaseName
import com.github.jibbo.norwegiantraining.main.MainActivity
import com.github.jibbo.norwegiantraining.main.description
import com.github.jibbo.norwegiantraining.main.message
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class WorkoutTimerAndroidService : Service(), WorkoutTimerService {

    @Inject
    lateinit var stateManager: WorkoutTimerStateManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val pendingSpeechQueue = mutableListOf<Pair<String, Boolean>>()

    private var countDownTimer: CountDownTimer? = null
    private var binder: WorkoutServiceBinder? = null

    private var lastSpokenSeconds = -1

    override val timerState: StateFlow<WorkoutTimerState>
        get() = stateManager.state

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")

        createNotificationChannel()
        initializeTTS()

        serviceScope.launch {
            stateManager.initialize()

            val state = stateManager.state.value
            if (state.workoutId != -1L && state.isTimerRunning) {
                Log.d(TAG, "Restoring timer from saved state")
                restoreTimerFromState()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_WORKOUT -> {
                val workoutId = intent.getLongExtra(EXTRA_WORKOUT_ID, -1L)
                if (workoutId != -1L) {
                    serviceScope.launch {
                        startWorkout(workoutId)
                    }
                }
            }

            ACTION_PHASE_TRANSITION -> {
                serviceScope.launch {
                    handlePhaseTransition()
                }
            }

            ACTION_PAUSE_TIMER -> {
                serviceScope.launch {
                    pauseTimer()
                }
            }

            ACTION_SKIP_PHASE -> {
                serviceScope.launch {
                    skipPhase()
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "onBind")
        if (binder == null) {
            binder = WorkoutServiceBinder(this)
        }
        return binder!!
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        countDownTimer?.cancel()
        tts?.stop()
        tts?.shutdown()
        serviceScope.cancel()
        super.onDestroy()
    }

    override suspend fun startWorkout(workoutId: Long) {
        Log.d(TAG, "Starting workout: $workoutId")
        stateManager.startWorkout(workoutId)
        startForegroundWithNotification()
    }

    override suspend fun startTimer() {
        Log.d(TAG, "Starting timer")
        stateManager.startTimer()

        val state = stateManager.state.value
        scheduleAlarm(state.targetTimeMillis, state.currentPhaseIndex)
        startCountdown(state.targetTimeMillis)
        updateNotification()

        announcePhaseStart(state.currentPhase.name)
    }

    override suspend fun pauseTimer() {
        Log.d(TAG, "Pausing timer")
        countDownTimer?.cancel()
        cancelAlarm()
        stateManager.pauseTimer()
        updateNotification()
    }

    override suspend fun skipPhase() {
        Log.d(TAG, "Skipping phase")
        countDownTimer?.cancel()
        cancelAlarm()
        stateManager.skipPhase()

        val state = stateManager.state.value
        if (state.isCompleted) {
            updateNotification()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } else {
            // Update notification to show new phase before starting timer
            updateNotification()
            // Automatically start the timer for the next phase
            startTimer()
        }
    }

    override suspend fun advanceToNextPhase() {
        Log.d(TAG, "Advancing to next phase")
        countDownTimer?.cancel()
        cancelAlarm()
        stateManager.moveToNextPhase()

        val state = stateManager.state.value
        if (state.isCompleted) {
            updateNotification()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } else {
            // Update notification to show new phase before starting timer
            updateNotification()
            // Automatically start the timer for the next phase
            startTimer()
        }
    }

    override suspend fun closeWorkout() {
        Log.d(TAG, "Closing workout")
        countDownTimer?.cancel()
        cancelAlarm()
        stateManager.closeWorkout()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun handlePhaseTransition() {
        Log.d(TAG, "Handling phase transition from alarm")
        countDownTimer?.cancel()
        cancelAlarm()
        stateManager.moveToNextPhase()

        val state = stateManager.state.value
        if (state.isCompleted) {
            updateNotification()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } else {
            // Automatically start the timer for the next phase
            startTimer()
        }
    }

    private fun restoreTimerFromState() {
        val state = stateManager.state.value
        if (state.isTimerRunning && state.targetTimeMillis > System.currentTimeMillis()) {
            Log.d(TAG, "Restoring countdown timer")
            startCountdown(state.targetTimeMillis)
            scheduleAlarm(state.targetTimeMillis, state.currentPhaseIndex)
        }
        startForegroundWithNotification()
    }

    private fun startCountdown(targetTimeMillis: Long) {
        countDownTimer?.cancel()

        val duration = (targetTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        if (duration == 0L) {
            serviceScope.launch {
                handlePhaseTransition()
            }
            return
        }

        lastSpokenSeconds = -1

        countDownTimer = object : CountDownTimer(duration, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                if (stateManager.shouldAnnounceCountdown()) {
                    val secondsRemaining = (millisUntilFinished / 1000).toInt()
                    announceCountdown(secondsRemaining)
                }
                updateNotification()
            }

            override fun onFinish() {
                serviceScope.launch {
                    handlePhaseTransition()
                }
            }
        }.start()
    }

    private fun announceCountdown(secondsRemaining: Int) {
        val shouldAnnounce = when (secondsRemaining) {
            60 -> {
                speak(getString(R.string.one_minute_remaining), flush = false)
                true
            }

            3 -> {
                speak(getString(R.string.three), flush = false)
                true
            }

            2 -> {
                speak(getString(R.string.two), flush = false)
                true
            }

            1 -> {
                speak(getString(R.string.one), flush = false)
                true
            }

            else -> false
        }

        if (shouldAnnounce) {
            lastSpokenSeconds = secondsRemaining
        }
    }

    private fun announcePhaseStart(phaseName: PhaseName) {
        if (stateManager.shouldAnnouncePhase()) {
            speak(getString(phaseName.message()), flush = true)
        }
        if (stateManager.shouldAnnouncePhaseDesc()) {
            speak(getString(phaseName.description()), flush = false)
        }
    }

    private fun initializeTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                isTtsReady = true
                Log.d(TAG, "TTS initialized successfully")
                processPendingSpeech()
            } else {
                Log.e(TAG, "TTS initialization failed")
                isTtsReady = false
            }
        }
    }

    private fun speak(text: String, flush: Boolean) {
        if (isTtsReady) {
            tts?.speak(
                text,
                if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                null,
                "timer_$text"
            )
        } else {
            pendingSpeechQueue.add(Pair(text, flush))
        }
    }

    private fun processPendingSpeech() {
        if (pendingSpeechQueue.isNotEmpty()) {
            pendingSpeechQueue.forEach { (text, flush) ->
                speak(text, flush)
            }
            pendingSpeechQueue.clear()
        }
    }

    private fun scheduleAlarm(targetTimeMillis: Long, phaseIndex: Int) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_PHASE_INDEX, phaseIndex)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetTimeMillis,
                    pendingIntent
                )
                Log.d(TAG, "Alarm scheduled for $targetTimeMillis")
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetTimeMillis, pendingIntent)
            Log.d(TAG, "Alarm scheduled for $targetTimeMillis")
        }
    }

    private fun cancelAlarm() {
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        if (pendingIntent != null) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Alarm cancelled")
        }
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification() {
        val notification = buildNotification()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(): Notification {
        val state = stateManager.state.value

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("workout_id", state.workoutId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pauseIntent = Intent(this, WorkoutTimerAndroidService::class.java).apply {
            action = ACTION_PAUSE_TIMER
        }
        val pausePendingIntent = PendingIntent.getService(
            this,
            1,
            pauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val skipIntent = Intent(this, WorkoutTimerAndroidService::class.java).apply {
            action = ACTION_SKIP_PHASE
        }
        val skipPendingIntent = PendingIntent.getService(
            this,
            2,
            skipIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val phaseName = getString(state.currentPhase.name.message())
        val remainingMillis = stateManager.getRemainingTimeMillis()
        val minutes = (remainingMillis / 1000 / 60).toInt()
        val seconds = ((remainingMillis / 1000) % 60).toInt()
        val timeText = String.format("%02d:%02d", minutes, seconds)

        val title = if (state.isTimerRunning && remainingMillis > 0) {
            "$timeText - $phaseName"
        } else if (state.remainingTimeOnPauseMillis > 0) {
            "$timeText (Paused) - $phaseName"
        } else {
            state.workoutName.ifEmpty { "Workout Timer" }
        }

        val contentText = if (state.isCompleted) {
            "Workout Completed!"
        } else {
            state.workoutName.ifEmpty { "Norwegian Training" }
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)  // Always non-dismissible during workout
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(false)

        if (!state.isCompleted && state.currentPhase.name != PhaseName.GET_READY) {
            if (state.isTimerRunning) {
                builder.addAction(
                    R.drawable.ic_launcher_foreground,
                    "Pause",
                    pausePendingIntent
                )
            }
            builder.addAction(
                R.drawable.ic_launcher_foreground,
                "Skip",
                skipPendingIntent
            )
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows workout timer progress"
                setShowBadge(false)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "WorkoutTimerService"
        private const val CHANNEL_ID = "workout_timer_channel"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_START_WORKOUT = "com.github.jibbo.norwegiantraining.ACTION_START_WORKOUT"
        const val ACTION_PHASE_TRANSITION =
            "com.github.jibbo.norwegiantraining.ACTION_PHASE_TRANSITION"
        const val ACTION_PAUSE_TIMER = "com.github.jibbo.norwegiantraining.ACTION_PAUSE_TIMER"
        const val ACTION_SKIP_PHASE = "com.github.jibbo.norwegiantraining.ACTION_SKIP_PHASE"

        const val EXTRA_WORKOUT_ID = "workout_id"
        const val EXTRA_PHASE_INDEX = "phase_index"
    }
}
