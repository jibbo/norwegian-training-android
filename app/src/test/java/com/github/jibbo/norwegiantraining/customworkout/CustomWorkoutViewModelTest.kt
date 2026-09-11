package com.github.jibbo.norwegiantraining.customworkout

import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutDraft
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutField
import com.github.jibbo.norwegiantraining.domain.CustomWorkoutValidationError
import com.github.jibbo.norwegiantraining.testutils.FakeWorkoutRepository
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

    private fun workout(id: Long) = Workout(
        id = id,
        name = "Existing",
        difficulty = Difficulty.BEGINNER,
        content = "5m-30s-15s-5m",
        isCustom = true,
    )
}
