package com.jna.tictactoe

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Test for verifying navigation flows in the Tic Tac Toe app.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testSplashTransitionsToMenu() {
        // 1. Initially, we should be on the Splash screen
        // "EST. MMXXIV" is only on the Splash screen.
        composeTestRule.onNodeWithText("EST. MMXXIV").assertExists()

        // 2. Wait for the transition to occur (SplashScreen has a 2s delay)
        // We use waitUntil to wait for the transition to the Menu screen.
        // We can check for a button that only exists on the Menu screen.
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasText("VS CPU")).fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Verify we are on the Menu screen
        composeTestRule.onNodeWithText("VS CPU").assertExists()
        composeTestRule.onNodeWithText("LOCAL MULTIPLAYER").assertExists()
        composeTestRule.onNodeWithText("WI-FI LAN").assertExists()

        // 4. Verify Splash content is gone (because we used popUpTo with inclusive = true)
        composeTestRule.onNodeWithText("EST. MMXXIV").assertDoesNotExist()
    }
}
