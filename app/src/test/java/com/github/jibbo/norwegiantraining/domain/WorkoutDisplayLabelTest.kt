package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutDisplayLabelTest {
    @Test
    fun `label includes icon and one separating space`() {
        val workout = workout(icon = "🔥", name = "Intervals")

        assertEquals("🔥 Intervals", workout.displayLabel())
    }

    @Test
    fun `label omits leading space when icon is empty or null`() {
        assertEquals("Intervals", workout(icon = "", name = "Intervals").displayLabel())
        assertEquals("Intervals", workout(icon = null, name = "Intervals").displayLabel())
    }

    private fun workout(icon: String?, name: String) = Workout(
        name = name,
        difficulty = Difficulty.BEGINNER,
        content = "5m-1m-1m-5m",
        isCustom = true,
        icon = icon,
    )
}
