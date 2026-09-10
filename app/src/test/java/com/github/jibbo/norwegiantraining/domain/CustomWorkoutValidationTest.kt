package com.github.jibbo.norwegiantraining.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomWorkoutValidationTest {
    @Test
    fun `valid draft trims name and parses all numeric fields`() {
        val result = validateCustomWorkoutDraft(
            CustomWorkoutDraft(
                name = "  Morning  ",
                icon = "",
                workMinutes = "1",
                workSeconds = "59",
                restMinutes = "0",
                restSeconds = "1",
                rounds = "99",
            ),
        )

        assertTrue(result.isValid)
        assertEquals("Morning", result.trimmedName)
        assertEquals("", result.icon)
        assertEquals(1, result.workMinutes)
        assertEquals(59, result.workSeconds)
        assertEquals(0, result.restMinutes)
        assertEquals(1, result.restSeconds)
        assertEquals(99, result.rounds)
    }

    @Test
    fun `empty or whitespace name is valid`() {
        assertTrue(validateCustomWorkoutDraft(CustomWorkoutDraft(name = "")).isValid)
        assertTrue(validateCustomWorkoutDraft(CustomWorkoutDraft(name = "   ")).isValid)
    }

    @Test
    fun `name longer than 20 trimmed characters is rejected`() {
        val result = validateCustomWorkoutDraft(CustomWorkoutDraft(name = "  123456789012345678901  "))

        assertEquals(
            CustomWorkoutValidationError.NAME_TOO_LONG,
            result.errors[CustomWorkoutField.NAME],
        )
    }

    @Test
    fun `duration parts reject blank non numeric negative and out of range values`() {
        val result = validateCustomWorkoutDraft(
            CustomWorkoutDraft(
                workMinutes = "",
                workSeconds = "abc",
                restMinutes = "-1",
                restSeconds = "60",
            ),
        )

        assertEquals(
            CustomWorkoutValidationError.INVALID_NUMBER,
            result.errors[CustomWorkoutField.WORK_MINUTES],
        )
        assertEquals(
            CustomWorkoutValidationError.INVALID_NUMBER,
            result.errors[CustomWorkoutField.WORK_SECONDS],
        )
        assertEquals(
            CustomWorkoutValidationError.OUT_OF_RANGE,
            result.errors[CustomWorkoutField.REST_MINUTES],
        )
        assertEquals(
            CustomWorkoutValidationError.OUT_OF_RANGE,
            result.errors[CustomWorkoutField.REST_SECONDS],
        )
        assertFalse(result.isValid)
    }

    @Test
    fun `zero work and rest durations are rejected independently`() {
        val result = validateCustomWorkoutDraft(
            CustomWorkoutDraft(
                workMinutes = "0",
                workSeconds = "0",
                restMinutes = "0",
                restSeconds = "0",
            ),
        )

        assertEquals(
            CustomWorkoutValidationError.ZERO_DURATION,
            result.errors[CustomWorkoutField.WORK_DURATION],
        )
        assertEquals(
            CustomWorkoutValidationError.ZERO_DURATION,
            result.errors[CustomWorkoutField.REST_DURATION],
        )
    }

    @Test
    fun `rounds must be between one and 99`() {
        val zero = validateCustomWorkoutDraft(CustomWorkoutDraft(rounds = "0"))
        val tooMany = validateCustomWorkoutDraft(CustomWorkoutDraft(rounds = "100"))
        val invalid = validateCustomWorkoutDraft(CustomWorkoutDraft(rounds = "x"))

        assertEquals(
            CustomWorkoutValidationError.OUT_OF_RANGE,
            zero.errors[CustomWorkoutField.ROUNDS],
        )
        assertEquals(
            CustomWorkoutValidationError.OUT_OF_RANGE,
            tooMany.errors[CustomWorkoutField.ROUNDS],
        )
        assertEquals(
            CustomWorkoutValidationError.INVALID_NUMBER,
            invalid.errors[CustomWorkoutField.ROUNDS],
        )
    }

    @Test
    fun `duplicate names and empty icons are valid`() {
        val first = validateCustomWorkoutDraft(CustomWorkoutDraft(name = "Same", icon = null))
        val second = validateCustomWorkoutDraft(CustomWorkoutDraft(name = "Same", icon = ""))

        assertTrue(first.isValid)
        assertTrue(second.isValid)
    }
}
