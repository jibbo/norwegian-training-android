package com.github.jibbo.norwegiantraining.data

import com.github.jibbo.norwegiantraining.testutils.FakeAnalytics
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsTest {
    @Test
    fun `manual workout event has the exact name and no properties`() {
        val analytics = FakeAnalytics()

        analytics.logManualWorkoutLogged()

        assertEquals(listOf("manual_workout_logged"), analytics.calls)
    }
}
