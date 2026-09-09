package com.github.jibbo.norwegiantraining.thankyou

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jibbo.norwegiantraining.ui.theme.NorwegianTrainingTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class ThankYouScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersExactCopyAndHeading() {
        composeRule.setContent {
            NorwegianTrainingTheme(darkTheme = true) { ThankYouScreen(onContinue = {}) }
        }

        composeRule.onNodeWithText("Thank you! Your journey starts now.").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Your support means a lot to me as a solo developer and helps me keep improving Norwy.",
        ).assertIsDisplayed()
        composeRule.onNodeWithText("LET'S GO!").assertIsDisplayed()
        composeRule.onNodeWithText("Thank you! Your journey starts now.")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Heading, Unit))
    }

    @Test
    fun continueCallsCallbackOnce() {
        var calls = 0
        composeRule.setContent {
            NorwegianTrainingTheme(darkTheme = true) { ThankYouScreen(onContinue = { calls++ }) }
        }

        composeRule.onNodeWithText("LET'S GO!").performClick()
        assertEquals(1, calls)
    }

    @Test
    fun celebrationIsDecorativeAndButtonHasMinimumHeight() {
        composeRule.setContent {
            NorwegianTrainingTheme(darkTheme = true) { ThankYouScreen(onContinue = {}) }
        }

        composeRule.onRoot().assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription))
        val button = composeRule.onNodeWithText("LET'S GO!")
        button.assertHasClickAction()
        val height = button.fetchSemanticsNode().layoutInfo.height
        val density = InstrumentationRegistry.getInstrumentation().targetContext.resources.displayMetrics.density
        assertTrue(height >= 56 * density)
    }

    @Test
    fun buttonRemainsReachableInCompactAndLargeFontLayouts() {
        composeRule.setContent {
            Box(Modifier.height(300.dp)) {
                ThankYouScreen(onContinue = {})
            }
        }
        composeRule.onNodeWithText("LET'S GO!").performScrollTo().assertIsDisplayed()

        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2f)) {
                ThankYouScreen(onContinue = {})
            }
        }
        composeRule.onNodeWithText("LET'S GO!").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun contentIsCappedAt560DpOnWideSurface() {
        composeRule.setContent {
            Box(Modifier.fillMaxSize().width(1000.dp)) {
                ThankYouScreen(onContinue = {})
            }
        }

        val width = composeRule.onNodeWithText("LET'S GO!").fetchSemanticsNode().layoutInfo.width
        val density = InstrumentationRegistry.getInstrumentation().targetContext.resources.displayMetrics.density
        assertTrue(width <= 560 * density)
    }
}
