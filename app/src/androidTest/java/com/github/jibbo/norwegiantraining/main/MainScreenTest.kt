package com.github.jibbo.norwegiantraining.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.Modifier
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jibbo.norwegiantraining.domain.Phase
import com.github.jibbo.norwegiantraining.domain.PhaseName
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun completedWorkoutShowsFullPhaseProgress() {
        val state = UiState(
            step = Phase(PhaseName.COMPLETED, 0L),
            totalPhases = 3,
        )

        composeRule.setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                Box(Modifier.fillMaxSize()) {
                    Column {
                        Instructions(state)
                    }
                }
            }
        }

        composeRule.onNodeWithText("3 of 3").assertIsDisplayed()
    }
}
