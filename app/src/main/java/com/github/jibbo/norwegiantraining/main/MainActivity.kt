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
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.jibbo.norwegiantraining.components.BaseActivity
import com.github.jibbo.norwegiantraining.home.HomeActivity
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
        if (checkNotificationPermission()) {
            boundServiceToWorkoutId()
        }
        checkActivityRecognitionPermission()
        checkExactAlarmPermission()
        observe()
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        val bindIntent = Intent(this, WorkoutTimerAndroidService::class.java)
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)
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
        if (mainViewModel.uiStates.value.isServiceBound) {
            unbindService(serviceConnection)
        }
    }

    private fun observe() {
        lifecycleScope.launch {
            mainViewModel.uiEvents.flowWithLifecycle(lifecycle).collect {
                when (it) {
                    is UiCommands.CLOSE -> {
                        val intent = Intent(this@MainActivity, HomeActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            }
        }
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

    private fun checkActivityRecognitionPermission() {
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
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager =
                ContextCompat.getSystemService(this, android.app.AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) {
                Intent().also {
                    it.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(it)
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
