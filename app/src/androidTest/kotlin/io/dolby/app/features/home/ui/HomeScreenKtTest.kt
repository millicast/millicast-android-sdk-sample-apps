package io.dolby.app.features.home.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.dolby.app.common.ui.MillicastTheme
import io.dolby.app.navigation.AppNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Timer
import kotlin.concurrent.schedule

class HomeScreenKtTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Main.immediate) // Needed in UI test in order to update states immediately
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun homeScreenUiTest() {
        composeTestRule.setContent {
            MillicastTheme {
                AppNavigation()
            }
        }
        composeTestRule.onNodeWithTag("Subscribe").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Publish").assertIsDisplayed()
    }

    @Test
    fun subscribeOptionsScreenUiTest() {
        composeTestRule.setContent {
            MillicastTheme {
                AppNavigation()
            }
        }
        composeTestRule.onNodeWithTag("Subscribe").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Publish").assertIsDisplayed()
        composeTestRule.onNodeWithText("SUBSCRIBE").performClick()
        composeTestRule.onNodeWithTag("Single View").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Multi View").assertIsDisplayed()
    }

    @Test
    fun subscribeMultiViewUiTest() {
        composeTestRule.setContent {
            MillicastTheme {
                AppNavigation()
            }
        }
        composeTestRule.onNodeWithTag("Subscribe").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Publish").assertIsDisplayed()
        composeTestRule.onNodeWithText("SUBSCRIBE").performClick()
        composeTestRule.onNodeWithTag("Single View").assertIsDisplayed()
        composeTestRule.onNodeWithTag("Multi View").assertIsDisplayed()

        composeTestRule.onNodeWithText("SUBSCRIBE MULTI VIEW").performClick()
        composeTestRule.waitUntilTimeout(5000)
        composeTestRule.waitUntil(3000, condition = {
            val semanitc = SemanticsMatcher.expectValue(SemanticsProperties.TestTag, "Playback Screen")
            composeTestRule.onAllNodes(semanitc, true).fetchSemanticsNodes().isNotEmpty()
        })

        composeTestRule.waitUntil(10000, condition = {
            val semanitc = SemanticsMatcher.expectValue(SemanticsProperties.TestTag, "CAM1")
            composeTestRule.onAllNodes(semanitc, true).fetchSemanticsNodes().isNotEmpty()
        })
        composeTestRule.waitUntilTimeout(20000) // Wait amount of seconds to verify a successful playback
        composeTestRule.onNodeWithTag("CAM1").assertIsDisplayed()
    }

    private fun ComposeContentTestRule.waitUntilTimeout(
        timeoutMillis: Long
    ) {
        AsyncTimer.start(timeoutMillis)
        this.waitUntil(
            condition = { AsyncTimer.expired },
            timeoutMillis = timeoutMillis + 1000
        )
    }
}

object AsyncTimer {
    var expired = false
    fun start(delay: Long = 1000) {
        expired = false
        Timer().schedule(delay) {
            expired = true
        }
    }
}
