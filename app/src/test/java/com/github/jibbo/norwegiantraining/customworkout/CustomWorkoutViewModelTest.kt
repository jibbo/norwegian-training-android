package com.github.jibbo.norwegiantraining.customworkout

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutDraft
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutField
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutValidationError
import com.github.jibbo.norwegiantraining.testutils.FakeWorkoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomWorkoutViewModelTest {
    @Test
    fun `null id selects create mode`() {
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepository())

        viewModel.initialize(null)

        assertEquals(CustomWorkoutFormMode.CREATE, viewModel.uiState.value.mode)
        assertEquals(null, viewModel.uiState.value.workoutId)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `workout id selects edit mode and preserves id`() = runTest {
        val repository = FakeWorkoutRepository()
        repository.insert(workout(42L))
        assertEquals("Existing", repository.getById(42L)?.name)
        val viewModel = CustomWorkoutViewModel(repository)

        viewModel.initialize(42L)?.join()

        assertEquals(CustomWorkoutFormMode.EDIT, viewModel.uiState.value.mode)
        assertEquals(42L, viewModel.uiState.value.workoutId)
        assertEquals("Existing", viewModel.uiState.value.draft.name)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `field updates retain invalid text and clear stale errors`() {
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepository())
        viewModel.updateRounds("not a number")

        assertEquals("not a number", viewModel.uiState.value.draft.rounds)
        assertFalse(viewModel.validate())
        assertEquals(
            CustomWorkoutValidationError.INVALID_NUMBER,
            viewModel.uiState.value.validationErrors[CustomWorkoutField.ROUNDS],
        )

        viewModel.updateRounds("8")

        assertEquals("8", viewModel.uiState.value.draft.rounds)
        assertTrue(viewModel.uiState.value.validationErrors.isEmpty())
    }

    @Test
    fun `missing edit id is represented as not found`() = runTest {
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepository())

        viewModel.initialize(99L)?.join()

        assertTrue(viewModel.uiState.value.notFound)
        assertEquals(CustomWorkoutPersistenceError.NOT_FOUND, viewModel.uiState.value.persistenceError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `valid create saves generated custom workout and returns success`() = runTest {
        val repository = FakeWorkoutRepository()
        val viewModel = CustomWorkoutViewModel(repository)
        viewModel.initialize(null)
        viewModel.updateDraft(
            CustomWorkoutDraft(
                name = "Morning",
                icon = "🔥",
                workMinutes = "1",
                restMinutes = "0",
                restSeconds = "30",
                rounds = "2",
            )
        )

        viewModel.save()?.join()

        val stored = repository.getCustomWorkouts().first().single()
        assertTrue(viewModel.uiState.value.saved)
        assertEquals("Morning", stored.name)
        assertEquals("🔥", stored.icon)
        assertEquals("5m-1m-30s-1m-30s-5m", stored.content)
        assertTrue(stored.isCustom)
    }

    @Test
    fun `invalid create retains errors and does not insert`() = runTest {
        val repository = FakeWorkoutRepository()
        val viewModel = CustomWorkoutViewModel(repository)
        viewModel.initialize(null)
        viewModel.updateRounds("0")

        assertEquals(null, viewModel.save())
        assertFalse(viewModel.uiState.value.saved)
        assertEquals(0, repository.getCustomWorkouts().first().size)
        assertEquals(CustomWorkoutValidationError.OUT_OF_RANGE, viewModel.uiState.value.validationErrors[CustomWorkoutField.ROUNDS])
    }

    private fun workout(id: Long) = Workout(
        id = id,
        name = "Existing",
        difficulty = Difficulty.BEGINNER,
        content = "5m-30s-15s-5m",
        isCustom = true,
    )
}
