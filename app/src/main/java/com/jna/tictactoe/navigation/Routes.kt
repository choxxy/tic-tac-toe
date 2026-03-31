package com.jna.tictactoe.navigation

import com.jna.tictactoe.game.model.Difficulty
import com.jna.tictactoe.game.model.GameMode
import kotlinx.serialization.Serializable

/**
 * Route for the Splash screen.
 */
@Serializable
object Splash

/**
 * Route for the Main Menu screen.
 */
@Serializable
object Menu

/**
 * Route for the Game Lobby screen.
 */
@Serializable
object Lobby

/**
 * Route for the Game screen.
 *
 * @param mode The selected game mode (e.g., VS_CPU, VS_HUMAN_LOCAL).
 * @param difficulty The selected difficulty level (optional, used for CPU mode).
 */
@Serializable
data class Game(
    val mode: GameMode,
    val difficulty: Difficulty? = null
)
