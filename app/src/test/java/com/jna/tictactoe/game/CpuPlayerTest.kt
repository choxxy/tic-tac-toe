package com.jna.tictactoe.game

import com.jna.tictactoe.game.model.*
import org.junit.Test
import org.junit.Assert.*

class CpuPlayerTest {

    @Test
    fun testEasyAiStrategy() {
        val board = List(9) { CellState.EMPTY }
        val state = GameState(board = board, currentTurn = Player.X)
        
        val move = CpuPlayer.getMove(state, Difficulty.EASY)
        
        assertNotNull(move)
        assertTrue(move!! in 0..8)
    }

    @Test
    fun testMediumAiWinsIfPossible() {
        // X X .
        // O . .
        // . . .
        // X should take index 2 to win
        val board = List(9) { CellState.EMPTY }.toMutableList().apply {
            this[0] = CellState.X
            this[1] = CellState.X
            this[3] = CellState.O
        }
        val state = GameState(board = board, currentTurn = Player.X)
        
        val move = CpuPlayer.getMove(state, Difficulty.MEDIUM)
        
        assertEquals(2, move)
    }

    @Test
    fun testMediumAiBlocksIfPossible() {
        // X X .
        // . . .
        // . . .
        // O's turn, O should block at index 2
        val board = List(9) { CellState.EMPTY }.toMutableList().apply {
            this[0] = CellState.X
            this[1] = CellState.X
        }
        val state = GameState(board = board, currentTurn = Player.O)
        
        val move = CpuPlayer.getMove(state, Difficulty.MEDIUM)
        
        assertEquals(2, move)
    }

    @Test
    fun testHardAiWinsIfPossible() {
        // X X .
        // O . .
        // . . .
        // X should take index 2 to win immediately
        val board = List(9) { CellState.EMPTY }.toMutableList().apply {
            this[0] = CellState.X
            this[1] = CellState.X
            this[3] = CellState.O
        }
        val state = GameState(board = board, currentTurn = Player.X)
        
        val move = CpuPlayer.getMove(state, Difficulty.HARD)
        
        assertEquals(2, move)
    }

    @Test
    fun testHardAiBlocksImmediateWin() {
        // O O .
        // X . .
        // . . .
        // X's turn, X should block at index 2
        val board = List(9) { CellState.EMPTY }.toMutableList().apply {
            this[0] = CellState.O
            this[1] = CellState.O
            this[3] = CellState.X
        }
        val state = GameState(board = board, currentTurn = Player.X)
        
        val move = CpuPlayer.getMove(state, Difficulty.HARD)
        
        assertEquals(2, move)
    }

    @Test
    fun testHardAiNeverLosesAgainstRandom() {
        // Run many games where Hard AI plays against random moves
        // Hard AI should NEVER lose
        repeat(100) {
            var state = GameState(mode = GameMode.VS_CPU)
            val cpuPlayer = Player.O
            val humanPlayer = Player.X
            
            while (state.phase == GamePhase.PLAYING) {
                if (state.currentTurn == humanPlayer) {
                    val available = state.board.indices.filter { state.board[it] == CellState.EMPTY }
                    state = GameEngine.applyMove(state, available.random())
                } else {
                    val move = CpuPlayer.getMove(state, Difficulty.HARD)
                    state = GameEngine.applyMove(state, move!!)
                }
            }
            
            // Phase should be WIN (CPU wins) or DRAW. Human must NOT win.
            if (state.phase == GamePhase.WIN) {
                val winLine = state.winLine!!
                val winner = state.board[winLine[0]]
                assertNotEquals("Human won!", CellState.X, winner)
            }
        }
    }
}
