package com.github.jibbo.norwegiantraining.data

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutTest {
    @Test
    fun parsesSplitAndDerivedValues() {
        val workout = Workout(
            id = 1,
            name = "Test",
            difficulty = Difficulty.BEGINNER,
            content = "10s-1m-30s"
        )

        assertEquals(listOf(10_000L, 60_000L, 30_000L), workout.getSplit())
        assertEquals(1, workout.totalTime)
        assertEquals(3, workout.totalPhases)
        assertEquals(0, workout.kCal)
    }
}
