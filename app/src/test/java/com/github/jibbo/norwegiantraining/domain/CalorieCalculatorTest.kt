package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.roundToInt

class CalorieCalculatorTest {
    @Test
    fun `uses the configured MET formula without rounding`() {
        assertEquals(686.0, calculateCalories(ManualWorkoutType.RUN, 60), 0.0)
        assertEquals(122.5, calculateCalories(ManualWorkoutType.STRENGTH_TRAINING, 30), 0.0)
        assertEquals(262.5, calculateCalories(ManualWorkoutType.CYCLING, 30), 0.0)
        assertEquals(210.0, calculateCalories(ManualWorkoutType.SWIMMING, 30), 0.0)
        assertEquals(122.5, calculateCalories(ManualWorkoutType.WALKING, 30), 0.0)
        assertEquals(280.0, calculateCalories(ManualWorkoutType.HIIT, 30), 0.0)
    }

    @Test
    fun `total rounds only after summing precise session values`() {
        val sessions = listOf(
            Session(activityType = "RUN", duration = 1),
            Session(activityType = "RUN", duration = 1),
        )

        assertEquals(22.866666666666667, calculateTotalCalories(sessions), 0.000000000001)
        assertEquals(23, calculateTotalCalories(sessions).roundToInt())
    }

    @Test
    fun `zero duration calculates to zero`() {
        assertEquals(0.0, calculateCalories(ManualWorkoutType.HIIT, 0), 0.0)
    }

    @Test
    fun `unknown persisted activity type uses HIIT calories`() {
        assertEquals(
            calculateCalories(ManualWorkoutType.HIIT, 30),
            calculateTotalCalories(listOf(Session(activityType = "unknown", duration = 30))),
            0.0,
        )
    }
}
