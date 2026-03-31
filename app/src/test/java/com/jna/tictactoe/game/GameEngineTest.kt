package com.jna.tictactoe.game

import com.jna.tictactoe.game.model.*
import org.junit.Test
import org.junit.Assert.*

class GameEngineTest {

    @Test
    fun testValidMove() {
        val state = GameState()
        assertTrue(GameEngine.isValidMove(state, 0))
    }

    @Test
    fun testMoveOnOccupiedCell() {
        var state = GameState()
        state = GameEngine.applyMove(state, 0)
        assertFalse(GameEngine.isValidMove(state, 0))
    }

    @Test
    fun testMoveWhenGameOver() {
        var state = GameState(phase = GamePhase.WIN)
        assertFalse(GameEngine.isValidMove(state, 0))
    }

    @Test
    fun testApplyMoveTogglesTurn() {
        var state = GameState()
        assertEquals(Player.X, state.currentTurn)
        
        state = GameEngine.applyMove(state, 0)
        assertEquals(Player.O, state.currentTurn)
        assertEquals(CellState.X, state.board[0])
        
        state = GameEngine.applyMove(state, 1)
        assertEquals(Player.X, state.currentTurn)
        assertEquals(CellState.O, state.board[1])
    }

    @Test
    fun testWinDetection() {
        // X X X
        // . . .
        // . . .
        val board = listOf(
            CellState.X, CellState.X, CellState.X,
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY,
            CellState.EMPTY, CellState.EMPTY, CellState.EMPTY
        )
        val winLine = GameEngine.checkWin(board)
        assertNotNull(winLine)
        assertEquals(listOf(0, 1, 2), winLine)
    }

    @Test
    fun testAllWinLines() {
        val winLines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        
        for (line in winLines) {
            val board = MutableList(9) { CellState.EMPTY }
            line.forEach { board[it] = CellState.X }
            val detected = GameEngine.checkWin(board)
            assertNotNull("Failed to detect win line $line", detected)
            assertEquals(line, detected)
        }
    }

    @Test
    fun testDrawDetection() {
        // X O X
        // X O O
        // O X X
        val board = listOf(
            CellState.X, CellState.O, CellState.X,
            CellState.X, CellState.O, CellState.O,
            CellState.O, CellState.X, CellState.X
        )
        assertNull(GameEngine.checkWin(board))
        assertTrue(GameEngine.checkDraw(board))
    }

    @Test
    fun testFullBoardWinIsNotDraw() {
        // X X X
        // O O .
        // . . .
        // Let's make it full
        // X X X
        // O O X
        // O X O
        val board = listOf(
            CellState.X, CellState.X, CellState.X,
            CellState.O, CellState.O, CellState.X,
            CellState.O, CellState.X, CellState.O
        )
        assertNotNull(GameEngine.checkWin(board))
        assertFalse(GameEngine.checkDraw(board))
    }

    @Test
    fun testGameEndPhase() {
        var state = GameState()
        // X O X
        // X O O
        // O X .
        state = GameEngine.applyMove(state, 0) // X
        state = GameEngine.applyMove(state, 1) // O
        state = GameEngine.applyMove(state, 2) // X
        state = GameEngine.applyMove(state, 4) // O
        state = GameEngine.applyMove(state, 3) // X
        state = GameEngine.applyMove(state, 5) // O
        state = GameEngine.applyMove(state, 7) // X
        state = GameEngine.applyMove(state, 6) // O
        
        assertEquals(GamePhase.PLAYING, state.phase)
        
        state = GameEngine.applyMove(state, 8) // X
        assertEquals(GamePhase.DRAW, state.phase)
    }
}
