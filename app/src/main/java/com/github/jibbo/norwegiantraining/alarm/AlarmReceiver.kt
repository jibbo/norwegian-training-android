package com.github.jibbo.norwegiantraining.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.github.jibbo.norwegiantraining.service.WorkoutTimerAndroidService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm received, starting service for phase transition")

        val serviceIntent = Intent(context, WorkoutTimerAndroidService::class.java).apply {
            action = WorkoutTimerAndroidService.ACTION_PHASE_TRANSITION
            val phaseIndex = intent.getIntExtra(WorkoutTimerAndroidService.EXTRA_PHASE_INDEX, -1)
            if (phaseIndex != -1) {
                putExtra(WorkoutTimerAndroidService.EXTRA_PHASE_INDEX, phaseIndex)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
