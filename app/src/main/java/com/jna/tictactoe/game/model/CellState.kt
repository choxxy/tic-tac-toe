package com.jna.tictactoe.game.model

import kotlinx.serialization.Serializable

@Serializable
enum class CellState {
    X, O, EMPTY
}
