package com.github.jibbo.norwegiantraining.customworkout

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.service.WorkoutTimerState
import com.github.jibbo.norwegiantraining.data.FakeWorkoutRepo
import com.github.jibbo.norwegiantraining.data.FakeWorkoutRepo.FakeWorkoutTimerManager
import com.github.jibbo.norwegiantraining.service.WorkoutTimerStateManager
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomWorkoutFormScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun createFormShowsDefaultsAndNoDeleteAction() {
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepo(), FakeWorkoutTimerManager())
        setContent(viewModel, null)

        composeRule.onNodeWithText("Create workout").assertIsDisplayed()
        composeRule.onNodeWithText("10").assertIsDisplayed()
        composeRule.onNodeWithText("👟").assertIsDisplayed()
        composeRule.onAllNodesWithText("🗑️").assertCountEquals(0)
    }

    @Test
    fun createFormClearsIcon() {
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepo(), FakeWorkoutTimerManager())
        setContent(viewModel, null)

        composeRule.onNodeWithText("X").performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("👟").assertCountEquals(0)
        composeRule.onNodeWithText("X").assertIsDisplayed()
    }

    @Test
    fun fieldsCanBeEditedAndBackCallsCallback() {
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepo(), FakeWorkoutTimerManager())
        var backPressed = false
        setContent(viewModel, null, onBack = { backPressed = true })

        composeRule.onNodeWithText("Name").performTextReplacement("Morning")
        composeRule.onNodeWithText("Morning").assertIsDisplayed()
        composeRule.onNodeWithText("back").performClick()

        check(backPressed)
    }

    @Test
    fun editFormShowsDeleteAction() {
        val repository = FakeWorkoutRepo()
        runBlocking {
            repository.insert(
                Workout(
                id = 42L,
                name = "Existing",
                difficulty = Difficulty.BEGINNER,
                content = "5m-30s-15s-5m",
                isCustom = true,
            )
            )
        }
        val viewModel = CustomWorkoutViewModel(repository, FakeWorkoutTimerManager())
        setContent(viewModel, 42L)

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Edit workout").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Delete custom workout").assertIsDisplayed()
    }

    @Test
    fun invalidRoundsTextIsRetainedAfterSave() {
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepo(), FakeWorkoutTimerManager())
        setContent(viewModel, null)

        composeRule.onNodeWithText("8").performTextReplacement("invalid")
        composeRule.onNodeWithText("Save").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("invalid").assertIsDisplayed()
    }

    @Test
    fun deleteConfirmationCanBeCancelled() {
        val repository = repositoryWithCustomWorkout()
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepo(), FakeWorkoutTimerManager())
        setContent(viewModel, 42L)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Delete custom workout").performClick()
        composeRule.onNodeWithText("Delete workout?").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").performClick()

        composeRule.onAllNodesWithText("Delete workout?").assertCountEquals(0)
        check(runBlocking { repository.getById(42L) != null })
    }

    @Test
    fun confirmedDeleteRemovesWorkoutAndCallsBack() {
        val repository = repositoryWithCustomWorkout()
        val viewModel = CustomWorkoutViewModel(repository, FakeWorkoutTimerManager())
        var backPressed = false
        setContent(viewModel, 42L, onBack = { backPressed = true })
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Delete custom workout").performClick()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.waitForIdle()

        check(backPressed)
        check(runBlocking { repository.getById(42L) == null })
    }

    @Test
    fun activeSaveShowsLocalizedBlockingAlert() {
        val timerStateManager = FakeWorkoutTimerManager()
        val viewModel = CustomWorkoutViewModel(FakeWorkoutRepo(), timerStateManager)
        viewModel.updateName("Active workout")
        setContent(
            viewModel,
            null,
        )

        composeRule.onNodeWithText("Save").performClick()
        composeRule.onNodeWithText("Workout in progress").assertIsDisplayed()
        composeRule.onNodeWithText("Finish or close the active workout before saving or deleting a custom workout.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("OK").performClick()
        composeRule.onAllNodesWithText("Workout in progress").assertCountEquals(0)
    }

    @Test
    fun activeDeleteShowsLocalizedBlockingAlert() {
        val viewModel = CustomWorkoutViewModel(repositoryWithCustomWorkout(),
            FakeWorkoutTimerManager())
        setContent(
            viewModel,
            42L,
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("Delete custom workout").performClick()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.onNodeWithText("Workout in progress").assertIsDisplayed()
        composeRule.onNodeWithText("OK").performClick()
        composeRule.onAllNodesWithText("Workout in progress").assertCountEquals(0)
    }

    private fun repositoryWithCustomWorkout(): FakeWorkoutRepo {
        val repository = FakeWorkoutRepo()
        runBlocking {
            repository.insert(
                Workout(
                    id = 42L,
                    name = "Existing",
                    difficulty = Difficulty.BEGINNER,
                    content = "5m-30s-15s-5m",
                    isCustom = true,
                ),
            )
        }
        return repository
    }

    private fun setContent(
        viewModel: CustomWorkoutViewModel,
        workoutId: Long?,
        onBack: () -> Unit = {},
    ) {
        composeRule.setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                CustomWorkoutFormScreen(
                    viewModel = viewModel,
                    workoutId = workoutId,
                    onBack = onBack,
                )
            }
        }
    }
}
