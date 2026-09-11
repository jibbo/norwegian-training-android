package com.github.jibbo.norwegiantraining.customworkout

import org.junit.Assert.assertEquals
import org.junit.Test

class CustomWorkoutViewModelTest {
    @Test
    fun `null id selects create mode`() {
        val viewModel = CustomWorkoutViewModel()

        viewModel.initialize(null)

        assertEquals(CustomWorkoutFormMode.CREATE, viewModel.uiState.value.mode)
        assertEquals(null, viewModel.uiState.value.workoutId)
    }

    @Test
    fun `workout id selects edit mode and preserves id`() {
        val viewModel = CustomWorkoutViewModel()

        viewModel.initialize(42L)

        assertEquals(CustomWorkoutFormMode.EDIT, viewModel.uiState.value.mode)
        assertEquals(42L, viewModel.uiState.value.workoutId)
    }
}
