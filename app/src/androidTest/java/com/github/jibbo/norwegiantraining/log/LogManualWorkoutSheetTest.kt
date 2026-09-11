package com.github.jibbo.norwegiantraining.log

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutDraft
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutField
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutType
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutUiState
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutValidationError
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogManualWorkoutSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun plusButtonOpensManualWorkoutSheet() {
        composeRule.setContent {
            var manualState by mutableStateOf(ManualWorkoutUiState())
            NorwegianTrainingTheme(darkTheme = true) {
                Logs(
                    innerPadding = androidx.compose.foundation.layout.PaddingValues(),
                    uiState = UiState.Loaded(emptyMap()),
                    todayStatsUiState = TodayStatsUiState.Hidden,
                    manualWorkoutUiState = manualState,
                    onOpenManualWorkout = {
                        manualState = manualState.copy(sheetVisible = true)
                    },
                    onDismissManualWorkout = {
                        manualState = manualState.copy(sheetVisible = false)
                    },
                    onSelectManualWorkoutType = {},
                    onUpdateManualWorkoutDate = {},
                    onUpdateManualWorkoutHours = {},
                    onUpdateManualWorkoutMinutes = {},
                    onSubmitManualWorkout = {},
                    onHideTodayStats = {},
                    onRequestPermissions = {},
                    onOpenHealthConnect = {},
                )
            }
        }

        composeRule.onNodeWithTag("add_manual_workout_button").performClick()

        composeRule.onNodeWithTag("manual_workout_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("manual_workout_type_CYCLING").assertIsDisplayed()
    }

    @Test
    fun formRendersDefaultsAndAllTypesAndTracksSelection() {
        var selectedType: ManualWorkoutType? = null
        composeRule.setContent {
            var manualState by mutableStateOf(
                ManualWorkoutUiState(
                    sheetVisible = true,
                    draft = ManualWorkoutDraft(),
                ),
            )
            NorwegianTrainingTheme(darkTheme = true) {
                Logs(
                    innerPadding = androidx.compose.foundation.layout.PaddingValues(),
                    uiState = UiState.Loaded(emptyMap()),
                    todayStatsUiState = TodayStatsUiState.Hidden,
                    manualWorkoutUiState = manualState,
                    onOpenManualWorkout = {},
                    onDismissManualWorkout = {},
                    onSelectManualWorkoutType = { type ->
                        selectedType = type
                        manualState = manualState.copy(draft = manualState.draft.copy(type = type))
                    },
                    onUpdateManualWorkoutDate = {},
                    onUpdateManualWorkoutHours = {},
                    onUpdateManualWorkoutMinutes = {},
                    onSubmitManualWorkout = {},
                    onHideTodayStats = {},
                    onRequestPermissions = {},
                    onOpenHealthConnect = {},
                )
            }
        }

        composeRule.onNodeWithTag("manual_workout_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("manual_workout_date").fetchSemanticsNode()
        composeRule.onNodeWithTag("manual_workout_hours").assertTextContains("01")
        composeRule.onNodeWithTag("manual_workout_minutes").assertTextContains("00")
        ManualWorkoutType.values().forEach { type ->
            composeRule.onNodeWithTag("manual_workout_type_${type.name}").fetchSemanticsNode()
        }

        composeRule.onNodeWithTag("manual_workout_type_SWIMMING").performClick()

        assert(selectedType == ManualWorkoutType.SWIMMING)
    }

    @Test
    fun missingTypeDisplaysFieldErrorAndSaveRemainsPresent() {
        composeRule.setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                Logs(
                    innerPadding = androidx.compose.foundation.layout.PaddingValues(),
                    uiState = UiState.Loaded(emptyMap()),
                    todayStatsUiState = TodayStatsUiState.Hidden,
                    manualWorkoutUiState = ManualWorkoutUiState(
                        sheetVisible = true,
                        fieldErrors = mapOf(
                            ManualWorkoutField.TYPE to ManualWorkoutValidationError.TYPE_REQUIRED,
                        ),
                    ),
                    onOpenManualWorkout = {},
                    onDismissManualWorkout = {},
                    onSelectManualWorkoutType = {},
                    onUpdateManualWorkoutDate = {},
                    onUpdateManualWorkoutHours = {},
                    onUpdateManualWorkoutMinutes = {},
                    onSubmitManualWorkout = {},
                    onHideTodayStats = {},
                    onRequestPermissions = {},
                    onOpenHealthConnect = {},
                )
            }
        }

        composeRule.onNodeWithTag("manual_workout_error_type").fetchSemanticsNode()
        composeRule.onNodeWithTag("manual_workout_save").fetchSemanticsNode()
    }
}