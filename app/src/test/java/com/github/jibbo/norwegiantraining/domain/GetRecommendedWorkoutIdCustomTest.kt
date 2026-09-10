package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.testutils.FakeSettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetRecommendedWorkoutIdCustomTest {
    @Test
    fun `custom rows are excluded from fallback recommendation`() {
        val settings = FakeSettingsRepository().apply {
            setFitnessLevel(FitnessLevel.BEGINNER)
        }

        val result = GetRecommendedWorkoutId(settings)(workoutsWithCustom())

        assertEquals(1L, result)
    }

    @Test
    fun `custom stored recommendation is cleared and ignored`() {
        val settings = FakeSettingsRepository().apply {
            setFitnessLevel(FitnessLevel.BEGINNER)
            setRecommendedWorkoutId(99L)
        }

        val result = GetRecommendedWorkoutId(settings)(workoutsWithCustom())

        assertEquals(1L, result)
        assertNull(settings.getRecommendedWorkoutId())
    }

    @Test
    fun `custom last workout cannot override built-in recommendation`() {
        val settings = FakeSettingsRepository().apply {
            setFitnessLevel(FitnessLevel.BEGINNER)
            setLastWorkoutId(99L)
        }

        val result = GetRecommendedWorkoutId(settings)(workoutsWithCustom())

        assertEquals(1L, result)
    }

    private fun workoutsWithCustom() = mapOf(
        Difficulty.BEGINNER to listOf(
            Workout(99, "Custom", Difficulty.BEGINNER, "5m-1m-1m-5m", isCustom = true),
            Workout(1, "Built-in", Difficulty.BEGINNER, "5m-1m-1m-5m"),
        ),
    )
}
