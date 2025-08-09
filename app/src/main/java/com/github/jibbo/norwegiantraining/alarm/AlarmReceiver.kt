package com.github.jibbo.norwegiantraining.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.jibbo.norwegiantraining.domain.MoveToNextPhaseDomainService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var getNextPhase: MoveToNextPhaseDomainService

    override fun onReceive(context: Context, intent: Intent) {
//        GlobalScope.launch {
//            val nextPhase = getNextPhase()
//            val triggerTime = System.currentTimeMillis() + nextPhase.durationMillis!!
//            AlarmUtils.showNotification(context, triggerTime)
//        }

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
