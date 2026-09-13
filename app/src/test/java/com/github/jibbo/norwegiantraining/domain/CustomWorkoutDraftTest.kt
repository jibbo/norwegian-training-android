package com.github.jibbo.norwegiantraining.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CustomWorkoutDraftTest {
    @Test
    fun `default draft contains configured form defaults`() {
        val draft = CustomWorkoutDraft()

        assertEquals("", draft.name)
        assertEquals("👟", draft.icon)
        assertEquals("10", draft.workMinutes)
        assertEquals("0", draft.workSeconds)
        assertEquals("5", draft.restMinutes)
        assertEquals("0", draft.restSeconds)
        assertEquals("8", draft.rounds)
    }

    @Test
    fun `draft preserves user-entered values`() {
        val draft = CustomWorkoutDraft(
            name = "  Morning  ",
            icon = null,
            workMinutes = "x",
            workSeconds = "60",
            restMinutes = "",
            restSeconds = "-1",
            rounds = "100",
        )

        assertEquals("  Morning  ", draft.name)
        assertNull(draft.icon)
        assertEquals("x", draft.workMinutes)
        assertEquals("60", draft.workSeconds)
        assertEquals("", draft.restMinutes)
        assertEquals("-1", draft.restSeconds)
        assertEquals("100", draft.rounds)
    }
}
