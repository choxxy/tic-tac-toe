package com.jna.tictactoe.screen.game

import com.jna.tictactoe.game.model.GameState

/**
 * UI State for the Game screen.
 *
 * @property gameState The current state of the game engine.
 * @property xWins Total wins for player X in the current session.
 * @property oWins Total wins for player O in the current session.
 * @property draws Total draws in the current session.
 * @property isThinking True if the AI is currently processing its move.
 */
data class GameUiState(
    val gameState: GameState = GameState(),
    val xWins: Int = 0,
    val oWins: Int = 0,
    val draws: Int = 0,
    val isThinking: Boolean = false
)
