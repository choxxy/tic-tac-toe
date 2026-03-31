package com.jna.tictactoe.game.model

import kotlinx.serialization.Serializable

/**
 * Represents the different modes the game can be played in.
 */
@Serializable
enum class GameMode {
    /** Playing against the AI. */
    VS_CPU,
    /** Playing against another person on the same device. */
    VS_HUMAN_LOCAL,
    /** Playing against another person over the local network. */
    VS_LAN
}
