package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.testutils.FakeAnalytics
import com.github.jibbo.norwegiantraining.testutils.FakeSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class SaveManualWorkoutUseCaseTest {
    private val today = LocalDate.of(2026, 9, 11)
    private val validation = validateManualWorkoutDraft(
        ManualWorkoutDraft(
            type = ManualWorkoutType.RUN,
            date = today,
            hours = "01",
            minutes = "30",
        ),
        today = today,
    )

    @Test
    fun `saves the required manual session payload and emits analytics`() = runTest {
        val repository = FakeSessionRepository()
        val analytics = FakeAnalytics()
        val result = SaveManualWorkoutUseCase(repository, analytics)(validation, "Run")

        assertTrue(result is ManualWorkoutSaveResult.Success)
        val session = repository.getSessions().single()
        assertEquals(0L, session.id)
        assertEquals(today, session.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
        assertTrue(session.isManual)
        assertEquals("Run", session.name)
        assertEquals(90L, session.duration)
        assertEquals(1, session.phasesEnded)
        assertEquals(0, session.skipCount)
        assertEquals(listOf("manual_workout_logged"), analytics.calls)
    }

    @Test
    fun `allows duplicate manual saves`() = runTest {
        val repository = FakeSessionRepository()
        val analytics = FakeAnalytics()
        val useCase = SaveManualWorkoutUseCase(repository, analytics)

        useCase(validation, "Run")
        useCase(validation, "Run")

        assertEquals(2, repository.getSessions().size)
        assertEquals(2, analytics.calls.size)
    }

    @Test
    fun `rejects invalid validation without persistence or analytics`() = runTest {
        val repository = FakeSessionRepository()
        val analytics = FakeAnalytics()
        val invalid = ManualWorkoutValidationResult(
            value = null,
            errors = mapOf(ManualWorkoutField.TYPE to ManualWorkoutValidationError.TYPE_REQUIRED),
        )

        val result = SaveManualWorkoutUseCase(repository, analytics)(invalid, "Run")

        assertEquals(ManualWorkoutSaveResult.InvalidInput, result)
        assertTrue(repository.getSessions().isEmpty())
        assertTrue(analytics.calls.isEmpty())
    }

    @Test
    fun `returns database failure and does not emit analytics`() = runTest {
        val repository = FakeSessionRepository().apply {
            manualInsertFailure = IllegalStateException("database unavailable")
        }
        val analytics = FakeAnalytics()

        val result = SaveManualWorkoutUseCase(repository, analytics)(validation, "Run")

        assertTrue(result is ManualWorkoutSaveResult.DatabaseFailure)
        assertTrue(analytics.calls.isEmpty())
    }
}
