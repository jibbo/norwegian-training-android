package com.github.jibbo.norwegiantraining.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm received")
        // Play alarm sound
//        val mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound)
//        mediaPlayer.start()

        // Vibrate
//        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
//        } else {
//            vibrator.vibrate(1000)
//        }


    }
}
