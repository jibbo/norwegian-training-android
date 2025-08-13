package com.github.jibbo.norwegiantraining.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.jibbo.norwegiantraining.R
import com.github.jibbo.norwegiantraining.main.MainActivity

object AlarmUtils {
    val CHANNEL_ID = "alarm_channel"
    val NOTIFICATION_ID = 1


    fun showNotification(
        context: Context,
        triggerTime: Long? = null,
        text: String? = null
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your notification icon
            .setContentTitle("Norwegian Training Alarm")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)

        if (triggerTime != null) {
            builder.setWhen(triggerTime)
                .setShowWhen(true)
                .setUsesChronometer(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChronometerCountDown(true)
            }

        } else if (text != null) {
            builder
                .setContentText(text)
                .setOngoing(true)
        }

        val notification = builder.build()
        return notification
    }

    fun dismissNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Channel"
            val descriptionText = "Channel for alarm notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
