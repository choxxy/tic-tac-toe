package com.jna.tictactoe.game.model

import kotlinx.serialization.Serializable

@Serializable
enum class GamePhase {
    PLAYING, WIN, DRAW
}
