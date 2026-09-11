package com.github.jibbo.norwegiantraining.data

import com.github.jibbo.norwegiantraining.testutils.FakeSessionRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class SessionRepositoryTest {
    @Test
    fun `manual inserts preserve duplicate entries`() = runBlocking {
        val repository = FakeSessionRepository()
        val date = Date(1234L)

        repository.insertManualSession(
            Session(
                date = date,
                isManual = true,
                name = "Run",
                duration = 30L,
                phasesEnded = 1,
            ),
        )
        repository.insertManualSession(
            Session(
                date = date,
                isManual = true,
                name = "Run",
                duration = 45L,
                phasesEnded = 1,
            ),
        )

        val sessions = repository.getSessions()
        assertEquals(2, sessions.size)
        assertEquals(listOf(30L, 45L), sessions.map { it.duration })
    }
}
