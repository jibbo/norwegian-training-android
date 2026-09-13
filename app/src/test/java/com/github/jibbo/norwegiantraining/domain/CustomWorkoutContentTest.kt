package com.github.jibbo.norwegiantraining.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CustomWorkoutContentTest {
    @Test
    fun `one round contains warmup work rest and cooldown`() {
        val content = generateCustomWorkoutContent(
            validateCustomWorkoutDraft(
                CustomWorkoutDraft(
                    workMinutes = "1",
                    workSeconds = "30",
                    restMinutes = "0",
                    restSeconds = "45",
                    rounds = "1",
                ),
            ),
        )

        assertEquals("5m-90s-45s-5m", content)
    }

    @Test
    fun `eight rounds contain eight work rest pairs and final rest before cooldown`() {
        val content = generateCustomWorkoutContent(
            validateCustomWorkoutDraft(CustomWorkoutDraft()),
        ) ?: error("Expected valid content")
        val phases = content.split("-")

        assertEquals(18, phases.size)
        assertEquals("5m", phases.first())
        assertEquals("5m", phases.last())
        assertEquals(8, phases.drop(1).dropLast(1).size / 2)
        assertEquals("10m", phases[15])
        assertEquals("5m", phases[16])
    }

    @Test
    fun `ninety nine rounds are generated without zero duration phases`() {
        val content = generateCustomWorkoutContent(
            validateCustomWorkoutDraft(
                CustomWorkoutDraft(
                    workMinutes = "0",
                    workSeconds = "1",
                    restMinutes = "0",
                    restSeconds = "2",
                    rounds = "99",
                ),
            ),
        ) ?: error("Expected valid content")
        val phases = content.split("-")

        assertEquals(200, phases.size)
        assertEquals(99, phases.count { it == "1s" })
        assertEquals(99, phases.count { it == "2s" })
        assertEquals(0, phases.count { it == "0s" || it == "0m" })
    }

    @Test
    fun `invalid validation result does not generate content`() {
        val validation = validateCustomWorkoutDraft(CustomWorkoutDraft(rounds = "0"))

        assertNull(generateCustomWorkoutContent(validation))
    }

    @Test
    fun `persisted content is parsed into form fields`() {
        assertEquals(
            ParsedWorkoutContent("1", "30", "0", "45", "2"),
            parseCustomWorkoutContent("5m-90s-45s-90s-45s-5m"),
        )
    }

    @Test
    fun `malformed or non-repeating content is rejected`() {
        assertNull(parseCustomWorkoutContent("not-a-workout"))
        assertNull(parseCustomWorkoutContent("5m-1m-30s-2m-30s-5m"))
    }
}
