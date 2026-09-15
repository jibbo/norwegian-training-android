package com.github.jibbo.norwegiantraining.log

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jibbo.norwegiantraining.data.Session
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogSelectedDayTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun emptyDayShowsDaySheet() {
        composeRule.setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                Logs(PaddingValues(), UiState.Loaded(emptyMap()), TodayStatsUiState.Hidden, {}, {}, {}, {})
            }
        }
        composeRule.onNodeWithTag("calendar_day_0_1").performClick()
        composeRule.onNodeWithTag("sessions_for_day_sheet").assertIsDisplayed()
    }

    @Test
    fun selectedDayPlusRequestsManualWorkoutForThatDate() {
        val day = Calendar.getInstance().apply {
            set(Calendar.MONTH, Calendar.JANUARY); set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.time
        var selectedDate: LocalDate? = null
        composeRule.setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                Logs(PaddingValues(), UiState.Loaded(mapOf(Calendar.JANUARY to listOf(Session(id = 1L, date = day, phasesEnded = 1)))), TodayStatsUiState.Hidden, { selectedDate = it }, {}, {}, {})
            }
        }
        composeRule.onNodeWithTag("calendar_day_0_15").performClick()
        composeRule.onNodeWithTag("add_manual_workout_for_day").performClick()
        assert(selectedDate == day.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate())
    }

    @Test
    fun todayToolbarScrollsToTodayAndOpensDaySheet() {
        composeRule.setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                Logs(PaddingValues(), UiState.Loaded(emptyMap()), TodayStatsUiState.Hidden, {}, {}, {}, {})
            }
        }

        val today = LocalDate.now()
        composeRule.onNodeWithTag("today").performClick()
        composeRule.onNodeWithTag("sessions_for_day_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("calendar_day_${today.monthValue - 1}_${today.dayOfMonth}").assertIsDisplayed()
    }

    @Test
    fun legacySessionShowsOnlyItsName() {
        val today = Date()
        composeRule.setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                Logs(
                    PaddingValues(),
                    UiState.Loaded(
                        mapOf(Calendar.getInstance().get(Calendar.MONTH) to listOf(
                            Session(date = today, name = "HIIT", duration = 30L),
                        )),
                    ),
                    TodayStatsUiState.Hidden,
                    {}, {}, {}, {},
                )
            }
        }

        composeRule.onNodeWithTag("today").performClick()
        composeRule.onNodeWithTag("sessions_for_day_sheet").assertIsDisplayed()
        composeRule.onNodeWithText("HIIT").assertIsDisplayed()
    }

}
