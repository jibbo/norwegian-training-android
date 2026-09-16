package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutDisplayLabelTest {
    @Test
    fun `label contains workout name`() {
        val workout = workout(name = "Intervals")

        assertEquals("Intervals", workout.displayLabel())
    }

    private fun workout(name: String) = Workout(
        name = name,
        difficulty = Difficulty.BEGINNER,
        content = "5m-1m-1m-5m",
        isCustom = true,
    )
}
