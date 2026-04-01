package com.jna.tictactoe.screen.game

import com.jna.tictactoe.game.model.GameMode
import com.jna.tictactoe.game.model.GameState
import com.jna.tictactoe.game.model.Player

/**
 * UI State for the Game screen.
 *
 * @property gameState The current state of the game engine.
 * @property xWins Total wins for player X in the current session.
 * @property oWins Total wins for player O in the current session.
 * @property draws Total draws in the current session.
 * @property isThinking True if the AI is currently processing its move.
 * @property isHost True if this player is the host in LAN mode.
 * @property peerName The name of the opponent in LAN mode.
 */
data class GameUiState(
    val gameState: GameState = GameState(),
    val xWins: Int = 0,
    val oWins: Int = 0,
    val draws: Int = 0,
    val isThinking: Boolean = false,
    val isHost: Boolean = true,
    val peerName: String? = null,
    val isReconnecting: Boolean = false,
    val reconnectCountdown: Int? = null
) {
    /**
     * True if it's currently the opponent's turn in a LAN game.
     */
    val isWaitingForPeerMove: Boolean
        get() = if (gameState.mode == GameMode.VS_LAN) {
            if (isHost) {
                gameState.currentTurn == Player.O
            } else {
                gameState.currentTurn == Player.X
            }
        } else {
            false
        }
}
