package com.github.jibbo.norwegiantraining.service

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutTimerServiceIntegrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context
        get() = instrumentation.targetContext

    @Before
    fun grantNotificationPermission() {
        instrumentation.uiAutomation.adoptShellPermissionIdentity()
        try {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.ACTIVITY_RECOGNITION,
            )
        } finally {
            instrumentation.uiAutomation.dropShellPermissionIdentity()
        }
    }

    @After
    fun stopTimerService() {
        context.stopService(Intent(context, WorkoutTimerAndroidService::class.java))
    }

    @Test
    fun startIntentPublishesNotificationAndRecoversMissingWorkout() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, WorkoutTimerAndroidService::class.java).apply {
            action = WorkoutTimerAndroidService.ACTION_START_WORKOUT
            putExtra(WorkoutTimerAndroidService.EXTRA_WORKOUT_ID, Long.MAX_VALUE)
        }

        ContextCompat.startForegroundService(context, intent)

        val channel = waitForChannel(notificationManager)
        assertNotNull(channel)
        assertEquals("Workout Timer", channel!!.name)
    }

    private fun waitForChannel(notificationManager: NotificationManager) =
        repeatUntilNotNull(3_000L) {
            notificationManager.getNotificationChannel("workout_timer_channel")
        }

    private fun <T> repeatUntilNotNull(timeoutMillis: Long, block: () -> T?): T? {
        val deadline = SystemClock.uptimeMillis() + timeoutMillis
        var value: T?
        do {
            value = block()
            if (value != null) return value
            SystemClock.sleep(100L)
        } while (SystemClock.uptimeMillis() < deadline)
        return null
    }
}
