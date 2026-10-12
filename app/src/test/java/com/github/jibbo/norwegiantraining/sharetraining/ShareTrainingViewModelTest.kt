package com.github.jibbo.norwegiantraining.sharetraining

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareTrainingViewModelTest {
    @Test
    fun initializeBuildsWorkoutShareState() {
        val viewModel = ShareTrainingViewModel()

        viewModel.initialize(
            workoutName = "Beginner 1",
            completedAtMillis = 1_700_000_000_000L,
            totalPhases = 6,
            workoutDurationMinutes = 30,
        )

        val state = viewModel.uiStates.value
        assertEquals("Beginner 1", state.workoutName)
        assertEquals(1_700_000_000_000L, state.completedAtMillis)
        assertEquals(6, state.totalPhases)
        assertEquals(30, state.workoutDurationMinutes)
        assertFalse(state.canShare)
        assertTrue(state.formattedDate.isNotBlank())
    }

    @Test
    fun backgroundSelectionEnablesShare() {
        val viewModel = ShareTrainingViewModel()
        viewModel.initialize("Workout", 1_700_000_000_000L, 4, 20)

        viewModel.onBackgroundSelected("content://image/1")

        assertEquals("content://image/1", viewModel.uiStates.value.backgroundUri)
        assertTrue(viewModel.uiStates.value.canShare)
    }

    @Test
    fun initializeSanitizesInvalidValues() {
        val viewModel = ShareTrainingViewModel()

        viewModel.initialize(null, -1L, -3, -5)

        val state = viewModel.uiStates.value
        assertEquals("", state.workoutName)
        assertTrue(state.completedAtMillis > 0L)
        assertEquals(0, state.totalPhases)
        assertEquals(0, state.workoutDurationMinutes)
    }
}
