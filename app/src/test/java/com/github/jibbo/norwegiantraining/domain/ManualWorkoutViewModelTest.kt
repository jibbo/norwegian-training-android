package com.github.jibbo.norwegiantraining.domain

import com.github.jibbo.norwegiantraining.testutils.FakeAnalytics
import com.github.jibbo.norwegiantraining.testutils.FakeSessionRepository
import com.github.jibbo.norwegiantraining.testutils.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ManualWorkoutViewModelTest {
    @org.junit.Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()
    @Test
    fun `open resets form to defaults`() {
        val viewModel = ManualWorkoutViewModel(
            SaveManualWorkoutUseCase(FakeSessionRepository(), FakeAnalytics()),
        )
        val date = LocalDate.of(2026, 9, 11)

        viewModel.open(date)

        val state = viewModel.uiState.value
        assertTrue(state.sheetVisible)
        assertEquals(ManualWorkoutDraft(date = date), state.draft)
        assertTrue(state.fieldErrors.isEmpty())
    }

    @Test
    fun `field updates preserve other values`() {
        val viewModel = ManualWorkoutViewModel(
            SaveManualWorkoutUseCase(FakeSessionRepository(), FakeAnalytics()),
        )
        val date = LocalDate.of(2026, 9, 11)
        viewModel.open(date)

        viewModel.selectType(ManualWorkoutType.CYCLING)
        viewModel.updateDate(date.minusDays(1))
        viewModel.updateHours("02")
        viewModel.updateMinutes("15")

        assertEquals(
            ManualWorkoutDraft(
                type = ManualWorkoutType.CYCLING,
                date = date.minusDays(1),
                hours = "02",
                minutes = "15",
            ),
            viewModel.uiState.value.draft,
        )
    }

    @Test
    fun `invalid submit keeps sheet open and exposes field errors`() {
        val viewModel = ManualWorkoutViewModel(
            SaveManualWorkoutUseCase(FakeSessionRepository(), FakeAnalytics()),
        )
        viewModel.open(LocalDate.now())

        viewModel.submit("Run")

        val state = viewModel.uiState.value
        assertTrue(state.sheetVisible)
        assertFalse(state.isSaving)
        assertEquals(
            ManualWorkoutValidationError.TYPE_REQUIRED,
            state.fieldErrors[ManualWorkoutField.TYPE],
        )
    }

    @Test
    fun `database failure preserves input and keeps sheet open`() = runTest {
        val repository = FakeSessionRepository().apply {
            manualInsertFailure = IllegalStateException("database unavailable")
        }
        val viewModel = ManualWorkoutViewModel(
            SaveManualWorkoutUseCase(repository, FakeAnalytics()),
        )
        val date = LocalDate.now()
        viewModel.open(date)
        viewModel.selectType(ManualWorkoutType.RUN)
        viewModel.updateHours("02")

        viewModel.submit("Run")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.sheetVisible)
        assertFalse(state.isSaving)
        assertTrue(state.persistenceError)
        assertEquals("02", state.draft.hours)
    }

    @Test
    fun `successful submit closes sheet and emits completion`() = runTest {
        val repository = FakeSessionRepository()
        val analytics = FakeAnalytics()
        val viewModel = ManualWorkoutViewModel(
            SaveManualWorkoutUseCase(repository, analytics),
        )
        viewModel.open(LocalDate.now())
        viewModel.selectType(ManualWorkoutType.RUN)

        viewModel.submit("Run")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.sheetVisible)
        assertFalse(state.isSaving)
        assertTrue(state.saveCompleted)
        assertEquals(listOf("manual_workout_logged"), analytics.calls)
    }

    @Test
    fun `duplicate submit while saving is ignored`() = runTest {
        val repository = FakeSessionRepository()
        val analytics = FakeAnalytics()
        val viewModel = ManualWorkoutViewModel(
            SaveManualWorkoutUseCase(repository, analytics),
        )
        viewModel.open(LocalDate.now())
        viewModel.selectType(ManualWorkoutType.RUN)

        viewModel.submit("Run")
        viewModel.submit("Run")
        advanceUntilIdle()

        assertEquals(1, repository.getSessions().size)
        assertEquals(1, analytics.calls.size)
    }
}
