package com.github.jibbo.norwegiantraining.onboarding

import com.github.jibbo.norwegiantraining.domain.FitnessLevel
import com.github.jibbo.norwegiantraining.testutils.FakeSettingsRepository
import com.github.jibbo.norwegiantraining.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun continuesThroughPagesAndPersistsSelections() = runTest {
        val settings = FakeSettingsRepository()
        val viewModel = OnboardingViewModel(settings)

        viewModel.onNameChanged("Didi")
        viewModel.onFitnessLevelSelected(FitnessLevel.OCCASIONAL)
        viewModel.continueClicked(0)
        advanceUntilIdle()

        assertEquals("Didi", settings.getUserName())
        assertEquals(FitnessLevel.OCCASIONAL, settings.getFitnessLevel())
        assertEquals(2, viewModel.uiSelectedPage.value)
    }

    @Test
    fun permissionStepEmitsPermissionRequest() = runTest {
        val settings = FakeSettingsRepository()
        val viewModel = OnboardingViewModel(settings)

        val pages = OnboardingViewModel.OnboardingStates.getOnboardingPages()
        val permissionIndex = pages.indexOfFirst { it is OnboardingPage.Permission }

        if (permissionIndex >= 0) {
            viewModel.continueClicked(permissionIndex)
            advanceUntilIdle()
            assertTrue(viewModel.uiEvents.replayCache.last() is UiCommands.AskPermission)
        }
    }
}
