package com.github.jibbo.norwegiantraining.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jibbo.norwegiantraining.data.Difficulty
import com.github.jibbo.norwegiantraining.data.FakeSessionRepo
import com.github.jibbo.norwegiantraining.data.FakeSettingsRepository
import com.github.jibbo.norwegiantraining.data.FakeTracker
import com.github.jibbo.norwegiantraining.data.FakeWorkoutRepo
import com.github.jibbo.norwegiantraining.data.Workout
import com.github.jibbo.norwegiantraining.domain.GetAllWorkouts
import com.github.jibbo.norwegiantraining.domain.GetRecommendedWorkoutId
import com.github.jibbo.norwegiantraining.domain.GetUsername
import com.github.jibbo.norwegiantraining.domain.GetWeeklySessionsUseCase
import com.github.jibbo.norwegiantraining.domain.IsFreeTrial
import com.github.jibbo.norwegiantraining.domain.IsOnboardingCompleted
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeWorkoutsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeShowsSectionsAndSeparatesCustomFromBuiltIns() {
        val repository = repositoryWithWorkouts()
        val viewModel = homeViewModel(repository)
        setContent(viewModel)
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Your workouts").assertIsDisplayed()
        composeRule.onNodeWithText("All Workouts").assertIsDisplayed()
        composeRule.onNodeWithText("+").assertIsDisplayed()
        composeRule.onNodeWithText("Recommended", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("⭐ Newer").assertIsDisplayed()
        composeRule.onNodeWithText("🧘 Older").assertIsDisplayed()
        composeRule.onNodeWithText("Built-in").assertIsDisplayed()
        composeRule.onAllNodesWithText("Built-in").assertCountEquals(1)

        val newerTop = composeRule.onNodeWithText("⭐ Newer").fetchSemanticsNode().boundsInRoot.top
        val olderTop = composeRule.onNodeWithText("🧘 Older").fetchSemanticsNode().boundsInRoot.top
        check(newerTop < olderTop)
    }

    @Test
    fun customCardsExposeLongPressEditAndRenderIconNames() {
        val viewModel = homeViewModel(repositoryWithWorkouts())
        setContent(viewModel)
        composeRule.waitForIdle()

        composeRule.onNodeWithText("⭐ Newer")
            .performTouchInput { longClick() }
    }

    private fun setContent(viewModel: HomeViewModel) {
        composeRule.setContent {
            NorwegianTrainingTheme(darkTheme = true) {
                Workouts(viewModel)
            }
        }
    }

    private fun homeViewModel(repository: FakeWorkoutRepo): HomeViewModel {
        val settings = FakeSettingsRepository()
        return HomeViewModel(
            getUsername = GetUsername(settings),
            getAllWorkouts = GetAllWorkouts(repository),
            isFreeTrial = IsFreeTrial(settings),
            isOnboardingCompleted = IsOnboardingCompleted(settings),
            getRecommendedWorkoutId = GetRecommendedWorkoutId(settings),
            getWeeklySessions = GetWeeklySessionsUseCase(FakeSessionRepo()),
            analytics = FakeTracker(),
        )
    }

    private fun repositoryWithWorkouts(): FakeWorkoutRepo {
        val repository = FakeWorkoutRepo()
        runBlocking {
            repository.insert(
                Workout(
                    id = 1L,
                    name = "Recommended",
                    difficulty = Difficulty.BEGINNER,
                    content = "5m-30s-15s-5m",
                ),
            )
            repository.insert(
                Workout(
                    id = 2L,
                    name = "Older",
                    difficulty = Difficulty.BEGINNER,
                    content = "5m-30s-15s-5m",
                    isCustom = true,
                    icon = "🧘",
                ),
            )
            repository.insert(
                Workout(
                    id = 3L,
                    name = "Newer",
                    difficulty = Difficulty.BEGINNER,
                    content = "5m-30s-15s-5m",
                    isCustom = true,
                    icon = "⭐",
                ),
            )
            repository.insert(
                Workout(
                    id = 4L,
                    name = "Built-in",
                    difficulty = Difficulty.BEGINNER,
                    content = "5m-30s-15s-5m",
                ),
            )
        }
        return repository
    }
}
