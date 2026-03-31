package com.jna.tictactoe.screen.game

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jna.tictactoe.game.model.Difficulty
import com.jna.tictactoe.game.model.GameMode
import org.junit.Rule
import org.junit.Test

/**
 * Instrumentation tests for [GameScreen].
 * Verifies that the UI elements are correctly displayed on the screen.
 */
class GameScreenTest {

    /**
     * Compose test rule used to interact with the UI.
     */
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Verifies that the placeholder text is displayed with the correct mode and difficulty.
     */
    @Test
    fun gameScreen_displaysPlaceholderContent() {
        // Given
        val mode = GameMode.VS_CPU
        val difficulty = Difficulty.EASY

        // When
        composeTestRule.setContent {
            GameScreen(mode = mode, difficulty = difficulty)
        }

        // Then
        composeTestRule
            .onNodeWithText("Game Screen - Mode: VS_CPU, Difficulty: EASY")
            .assertExists()
    }
}
