package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutToPhasesConverterTest {
    @Test
    fun `converts valid content into safe phase list`() {
        val result = WorkoutToPhasesConverter.convert(workout("5m-90s-45s-5m"))

        assertTrue(result.isSuccess)
        assertEquals(
            listOf(
                PhaseName.GET_READY,
                PhaseName.WARMUP,
                PhaseName.HARD_PHASE,
                PhaseName.SOFT_PHASE,
                PhaseName.REST_PHASE,
                PhaseName.COMPLETED,
            ),
            result.getOrThrow().map { it.name },
        )
    }

    @Test
    fun `reports malformed content instead of throwing`() {
        val result = WorkoutToPhasesConverter.convert(workout("5m-not-a-duration-5m"))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is MalformedWorkoutException)
    }

    @Test
    fun `reports content with too few phases`() {
        val result = WorkoutToPhasesConverter.convert(workout("5m"))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is MalformedWorkoutException)
    }

    private fun workout(content: String) = Workout(
        name = "Workout",
        difficulty = Difficulty.BEGINNER,
        content = content,
    )
}
