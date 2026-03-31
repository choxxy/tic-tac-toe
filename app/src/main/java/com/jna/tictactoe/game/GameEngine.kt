package com.jna.tictactoe.game

import com.jna.tictactoe.game.model.*

/**
 * Core game engine for Tic-Tac-Toe, providing logic for move validation,
 * state transitions, and determining game outcomes (win/draw).
 */
object GameEngine {
    
    /**
     * Checks if a move is valid given the current game state and target cell index.
     * 
     * @param state The current [GameState].
     * @param index The target board index (0-8).
     * @return True if the move is within bounds, the game is in progress, and the cell is empty.
     */
    fun isValidMove(state: GameState, index: Int): Boolean {
        if (index !in 0..8) return false
        if (state.phase != GamePhase.PLAYING) return false
        if (state.board[index] != CellState.EMPTY) return false
        return true
    }

    /**
     * Applies a move to the current state and returns the updated [GameState].
     * 
     * @param state The current [GameState].
     * @param index The target board index (0-8).
     * @return A new [GameState] with the updated board and next player's turn, 
     * or the same state if the move is invalid.
     */
    fun applyMove(state: GameState, index: Int): GameState {
        if (!isValidMove(state, index)) return state

        val newBoard = state.board.toMutableList().apply {
            this[index] = when (state.currentTurn) {
                Player.X -> CellState.X
                Player.O -> CellState.O
                Player.NONE -> CellState.EMPTY
            }
        }

        return updateGameState(state.copy(board = newBoard))
    }

    /**
     * Determines the game phase and next player based on the current board.
     * 
     * @param state The current [GameState] with an updated board.
     * @return An updated [GameState] with correct phase, winning line, and current turn.
     */
    fun updateGameState(state: GameState): GameState {
        val winLine = checkWin(state.board)
        val phase = when {
            winLine != null -> GamePhase.WIN
            checkDraw(state.board) -> GamePhase.DRAW
            else -> GamePhase.PLAYING
        }

        // Only switch turns if the game is still in progress
        val nextTurn = if (phase == GamePhase.PLAYING) {
            if (state.currentTurn == Player.X) Player.O else Player.X
        } else {
            state.currentTurn
        }

        return state.copy(
            phase = phase,
            winLine = winLine,
            currentTurn = nextTurn
        )
    }

    /**
     * Scans the board for any winning combinations.
     * 
     * @param board The current list of [CellState].
     * @return A list of indices forming the winning line, or null if no winner exists.
     */
    fun checkWin(board: List<CellState>): List<Int>? {
        val winLines = listOf(
            // Rows
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            // Columns
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            // Diagonals
            listOf(0, 4, 8), listOf(2, 4, 6)
        )

        for (line in winLines) {
            val s1 = board[line[0]]
            val s2 = board[line[1]]
            val s3 = board[line[2]]
            // If all three cells in a line are occupied by the same player (non-empty)
            if (s1 != CellState.EMPTY && s1 == s2 && s1 == s3) {
                return line
            }
        }
        return null
    }

    /**
     * Checks if the game has ended in a draw (board is full with no winner).
     * 
     * @param board The current list of [CellState].
     * @return True if no moves remain and there is no winner.
     */
    fun checkDraw(board: List<CellState>): Boolean {
        if (checkWin(board) != null) return false
        return board.all { it != CellState.EMPTY }
    }
}
