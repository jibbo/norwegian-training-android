package com.github.jibbo.norwegiantraining.main

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.home.HomeActivity
import com.github.jibbo.norwegiantraining.levelup.LevelUpActivity
import com.github.jibbo.norwegiantraining.main.MainViewModel.UiCommands
import com.github.jibbo.norwegiantraining.service.WorkoutServiceBinder
import com.github.jibbo.norwegiantraining.service.WorkoutTimerAndroidService
import com.github.jibbo.norwegiantraining.service.WorkoutTimerService
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val REQUEST_CODE_POST_NOTIFICATIONS = 123
    private val REQUEST_CODE_ACTIVITY_RECOGNITION = 124

    private var timerService: WorkoutTimerService? = null
    private var serviceStartRequested = false
    private var serviceBindRequested = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d(TAG, "Service connected")
            timerService = (binder as WorkoutServiceBinder)
            mainViewModel.bindToService(binder)
            observeServiceState()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "Service disconnected")
            timerService = null
            serviceBindRequested = false
            mainViewModel.unbind()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                MainView(
                    mainViewModel = mainViewModel,
                )
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = closeToHome()
        })

        observe()
    }

    override fun onResume() {
        super.onResume()
        if(checkActivityRecognitionPermission()
            && checkNotificationPermission())
        {
            boundServiceToWorkoutId()
        }
        checkExactAlarmPermission()
    }

    private fun boundServiceToWorkoutId() {
        val workoutId = intent.getLongExtra("workout_id", -1L)
        if (workoutId > 0) {
            startAndBindService(workoutId)
        } else {
            Log.e(TAG, "Invalid workout ID: $workoutId")
        }
    }

    private fun startAndBindService(workoutId: Long) {
        val serviceIntent = Intent(this, WorkoutTimerAndroidService::class.java).apply {
            action = WorkoutTimerAndroidService.ACTION_START_WORKOUT
            putExtra(WorkoutTimerAndroidService.EXTRA_WORKOUT_ID, workoutId)
        }

        if (!serviceStartRequested) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            serviceStartRequested = true
        }

        if (!serviceBindRequested) {
            val bindIntent = Intent(this, WorkoutTimerAndroidService::class.java)
            serviceBindRequested = bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun observeServiceState() {
        lifecycleScope.launch {
            timerService?.timerState?.flowWithLifecycle(lifecycle)?.collect { state ->
                mainViewModel.updateFromService(state)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBindRequested) {
            unbindService(serviceConnection)
            serviceBindRequested = false
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            mainViewModel.uiEvents.flowWithLifecycle(lifecycle).collect { command ->
                when (command) {
                    is UiCommands.CLOSE -> closeToHome()
                    is UiCommands.LevelUp -> navigateTo(LevelUpActivity::class.java) {
                        putExtra(LevelUpActivity.EXTRA_NEW_LEVEL, command.newLevel.name)
                    }
                }
            }
        }
    }

    private fun closeToHome() {
        val left = intent.getFloatExtra(EXTRA_TRANSITION_LEFT, Float.NaN)
        val top = intent.getFloatExtra(EXTRA_TRANSITION_TOP, Float.NaN)
        val width = intent.getFloatExtra(EXTRA_TRANSITION_WIDTH, Float.NaN)
        val height = intent.getFloatExtra(EXTRA_TRANSITION_HEIGHT, Float.NaN)
        val view = window.decorView
        if (left.isNaN() || top.isNaN() || width <= 0f || height <= 0f) {
            finish()
            return
        }

        val targetWidth = width / view.width
        val targetHeight = height / view.height
        view.pivotX = left + width / 2f
        view.pivotY = top + height / 2f
        view.animate()
            .scaleX(targetWidth)
            .scaleY(targetHeight)
            .alpha(0f)
            .setDuration(280)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                finish()
                // The decor-view animation above is the complete exit transition.
                overridePendingTransition(0, 0)
            }
            .start()
    }

    private fun navigateTo(
        destination: Class<*>,
        configureIntent: Intent.() -> Unit = {}
    ) {
        val intent = Intent(this, destination).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            configureIntent()
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_POST_NOTIFICATIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Notification permission granted")
                    boundServiceToWorkoutId()
                }
            }

            REQUEST_CODE_ACTIVITY_RECOGNITION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Activity recognition permission granted")
                    checkNotificationPermission()
                }
            }
        }
    }

    private fun checkNotificationPermission(): Boolean {
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
                return false
            }
        }
        return true
    }

    private fun checkActivityRecognitionPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    REQUEST_CODE_ACTIVITY_RECOGNITION
                )
                return false
            }
        }
        return true
    }

    private fun checkExactAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager =
                ContextCompat.getSystemService(this, android.app.AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) {
                Intent().also {
                    it.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(it)
                }
                return false
            }
        }
        return true
    }

    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_TRANSITION_LEFT = "transition_left"
        const val EXTRA_TRANSITION_TOP = "transition_top"
        const val EXTRA_TRANSITION_WIDTH = "transition_width"
        const val EXTRA_TRANSITION_HEIGHT = "transition_height"
    }
}
