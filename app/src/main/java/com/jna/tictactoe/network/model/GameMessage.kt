package com.jna.tictactoe.network.model

import com.jna.tictactoe.game.model.GameState
import kotlinx.serialization.Serializable

@Serializable
sealed class GameMessage {
    @Serializable
    data class Handshake(val playerName: String) : GameMessage()

    @Serializable
    data class Move(val index: Int) : GameMessage()

    @Serializable
    data class SyncState(val state: GameState) : GameMessage()

    @Serializable
    data object Heartbeat : GameMessage()
}
