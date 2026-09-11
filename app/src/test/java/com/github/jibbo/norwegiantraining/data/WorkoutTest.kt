package com.github.jibbo.norwegiantraining.data

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutTest {
    @Test
    fun `existing workout defaults to built-in metadata`() {
        val workout = Workout(
            name = "Built-in",
            difficulty = Difficulty.BEGINNER,
            content = "5m-30s-5m",
        )

        assertEquals(false, workout.isCustom)
        assertEquals(null, workout.icon)
    }

    @Test
    fun `custom workout retains custom metadata`() {
        val workout = Workout(
            name = "Custom",
            difficulty = Difficulty.INTERMEDIATE,
            content = "5m-1m-1m-5m",
            isCustom = true,
            icon = "🔥",
        )

        assertEquals(true, workout.isCustom)
        assertEquals("🔥", workout.icon)
    }

    @Test
    fun parsesSplitAndDerivedValues() {
        val workout = Workout(
            id = 1,
            name = "Test",
            difficulty = Difficulty.BEGINNER,
            content = "10s-1m-30s",
        )

        assertEquals(listOf(10_000L, 60_000L, 30_000L), workout.getSplit())
        assertEquals(1, workout.totalTime)
        assertEquals(3, workout.totalPhases)
        assertEquals(0, workout.kCal)
    }
}
