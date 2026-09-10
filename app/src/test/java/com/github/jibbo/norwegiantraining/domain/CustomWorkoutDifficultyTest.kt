package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CustomWorkoutDifficultyTest {
    @Test
    fun `24 minutes 59 seconds is beginner`() {
        assertEquals(
            Difficulty.BEGINNER,
            difficulty(workMinutes = "14", workSeconds = "58", restSeconds = "1"),
        )
    }

    @Test
    fun `25 minutes is intermediate`() {
        assertEquals(
            Difficulty.INTERMEDIATE,
            difficulty(workMinutes = "14", workSeconds = "59", restSeconds = "1"),
        )
    }

    @Test
    fun `40 minutes is intermediate`() {
        assertEquals(
            Difficulty.INTERMEDIATE,
            difficulty(workMinutes = "29", workSeconds = "59", restSeconds = "1"),
        )
    }

    @Test
    fun `40 minutes 1 second is expert`() {
        assertEquals(
            Difficulty.EXPERT,
            difficulty(workMinutes = "30", workSeconds = "0", restSeconds = "1"),
        )
    }

    @Test
    fun `invalid validation result has no difficulty`() {
        val validation = validateCustomWorkoutDraft(CustomWorkoutDraft(rounds = "0"))

        assertNull(calculateCustomWorkoutDifficulty(validation))
    }

    private fun difficulty(
        workMinutes: String,
        workSeconds: String,
        restSeconds: String,
    ): Difficulty? = calculateCustomWorkoutDifficulty(
        validateCustomWorkoutDraft(
            CustomWorkoutDraft(
                workMinutes = workMinutes,
                workSeconds = workSeconds,
                restMinutes = "0",
                restSeconds = restSeconds,
                rounds = "1",
            ),
        ),
    )
}
