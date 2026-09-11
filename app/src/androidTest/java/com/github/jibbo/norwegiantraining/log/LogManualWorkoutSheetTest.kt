package com.github.jibbo.norwegiantraining.log

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jibbo.norwegiantraining.domain.ManualWorkoutUiState
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
}
