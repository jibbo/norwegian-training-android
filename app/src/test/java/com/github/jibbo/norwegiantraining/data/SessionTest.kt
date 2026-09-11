package com.github.jibbo.norwegiantraining.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class SessionTest {
    @Test
    fun `legacy defaults identify a non-manual HIIT session`() {
        val session = Session()

        assertFalse(session.isManual)
        assertEquals("HIIT", session.name)
        assertEquals(0L, session.duration)
    }

    @Test
    fun `manual session stores its metadata`() {
        val date = Date(1234L)
        val session = Session(
            phasesEnded = 1,
            skipCount = 0,
            date = date,
            isManual = true,
            name = "Run",
            duration = 90L,
        )

        assertTrue(session.isManual)
        assertEquals("Run", session.name)
        assertEquals(90L, session.duration)
        assertEquals(date, session.date)
        assertEquals(1, session.phasesEnded)
        assertEquals(0, session.skipCount)
    }
}
