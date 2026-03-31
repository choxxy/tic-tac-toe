package com.jna.tictactoe.game.model

import kotlinx.serialization.Serializable

/**
 * Represents the difficulty level of the AI opponent.
 */
@Serializable
enum class Difficulty {
    /** AI makes some random moves. */
    EASY,
    /** AI plays strategically but can be beaten. */
    MEDIUM,
    /** AI plays optimally, impossible to beat. */
    HARD
}
