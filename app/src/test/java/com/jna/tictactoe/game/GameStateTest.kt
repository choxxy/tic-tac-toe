package com.jna.tictactoe.game

import com.jna.tictactoe.game.model.CellState
import com.jna.tictactoe.game.model.GamePhase
import com.jna.tictactoe.game.model.GameState
import com.jna.tictactoe.game.model.Player
import org.junit.Test
import org.junit.Assert.*

class GameStateTest {

    @Test
    fun testInitialState() {
        val state = GameState()
        assertEquals(9, state.board.size)
        assertTrue(state.board.all { it == CellState.EMPTY })
        assertEquals(Player.X, state.currentTurn)
        assertEquals(GamePhase.PLAYING, state.phase)
        assertNull(state.winLine)
    }

    @Test
    fun testStateImmutability() {
        val state = GameState()
        val newState = state.copy(currentTurn = Player.O)
        
        assertEquals(Player.X, state.currentTurn)
        assertEquals(Player.O, newState.currentTurn)
        assertNotSame(state, newState)
    }
}
