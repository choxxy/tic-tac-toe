package com.jna.tictactoe.network.model

import com.jna.tictactoe.game.model.GameState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class GameMessageTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test Handshake serialization`() {
        val message: GameMessage = GameMessage.Handshake("Player1")
        val serialized = json.encodeToString(message)
        val deserialized = json.decodeFromString<GameMessage>(serialized)
        assertEquals(message, deserialized)
    }

    @Test
    fun `test Move serialization`() {
        val message: GameMessage = GameMessage.Move(4)
        val serialized = json.encodeToString(message)
        val deserialized = json.decodeFromString<GameMessage>(serialized)
        assertEquals(message, deserialized)
    }

    @Test
    fun `test SyncState serialization`() {
        val message: GameMessage = GameMessage.SyncState(GameState())
        val serialized = json.encodeToString(message)
        val deserialized = json.decodeFromString<GameMessage>(serialized)
        assertEquals(message, deserialized)
    }

    @Test
    fun `test Heartbeat serialization`() {
        val message: GameMessage = GameMessage.Heartbeat
        val serialized = json.encodeToString(message)
        val deserialized = json.decodeFromString<GameMessage>(serialized)
        assertEquals(message, deserialized)
    }
}
