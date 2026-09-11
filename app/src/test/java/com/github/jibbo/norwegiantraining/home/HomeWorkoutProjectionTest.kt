package com.github.jibbo.norwegiantraining.home

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeWorkoutProjectionTest {
    @Test
    fun `projection places recommended built-in first and customs newest first`() {
        val recommended = workout(2L, "Recommended")
        val olderCustom = workout(8L, "Older custom", isCustom = true)
        val newerCustom = workout(11L, "Newer custom", isCustom = true)
        val otherBuiltIn = workout(4L, "Other built-in")

        val projection = mapOf(
            Difficulty.BEGINNER to listOf(otherBuiltIn, newerCustom),
            Difficulty.EXPERT to listOf(recommended, olderCustom),
        ).toHomeWorkoutProjection(recommendedWorkoutId = 2L)

        assertEquals(recommended, projection.recommendedBuiltIn)
        assertEquals(listOf(newerCustom, olderCustom), projection.customWorkouts)
        assertEquals(listOf(otherBuiltIn), projection.remainingBuiltIns)
        assertEquals(listOf(recommended, newerCustom, olderCustom), projection.yourWorkouts)
        assertEquals(listOf(recommended, otherBuiltIn), projection.allBuiltIns)
    }

    @Test
    fun `custom workout cannot become the recommended workout`() {
        val custom = workout(9L, "Custom", isCustom = true)

        val projection = mapOf(Difficulty.BEGINNER to listOf(custom))
            .toHomeWorkoutProjection(recommendedWorkoutId = 9L)

        assertNull(projection.recommendedBuiltIn)
        assertEquals(listOf(custom), projection.customWorkouts)
        assertEquals(emptyList<Workout>(), projection.remainingBuiltIns)
    }

    private fun workout(id: Long, name: String, isCustom: Boolean = false) = Workout(
        id = id,
        name = name,
        difficulty = Difficulty.BEGINNER,
        content = "5m-30s-15s-5m",
        isCustom = isCustom,
    )
}
