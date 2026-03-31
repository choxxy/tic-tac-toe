package com.jna.tictactoe.game

import com.jna.tictactoe.game.model.*

/**
 * AI logic for the CPU opponent, supporting multiple difficulty levels.
 */
object CpuPlayer {

    /**
     * Calculates the best move for the CPU based on the current [GameState] and [Difficulty].
     * 
     * @param state The current game state.
     * @param difficulty The selected level of AI intelligence.
     * @return The chosen board index (0-8), or null if the game is over or the board is full.
     */
    fun getMove(state: GameState, difficulty: Difficulty): Int? {
        if (state.phase != GamePhase.PLAYING) return null

        val availableMoves = state.board.indices.filter { state.board[it] == CellState.EMPTY }
        if (availableMoves.isEmpty()) return null

        return when (difficulty) {
            Difficulty.EASY -> getEasyMove(availableMoves)
            Difficulty.MEDIUM -> getMediumMove(state, availableMoves)
            Difficulty.HARD -> getHardMove(state)
        }
    }

    /**
     * Simply picks a random available move.
     */
    private fun getEasyMove(availableMoves: List<Int>): Int {
        return availableMoves.random()
    }

    /**
     * Attempts to win or block a human win before picking a random move.
     */
    private fun getMediumMove(state: GameState, availableMoves: List<Int>): Int {
        val cpuPlayer = state.currentTurn
        val humanPlayer = if (cpuPlayer == Player.X) Player.O else Player.X

        // 1. Can CPU win in one move?
        findWinningMove(state.board, cpuPlayer)?.let { return it }

        // 2. Can Human win in one move? Block it.
        findWinningMove(state.board, humanPlayer)?.let { return it }

        // 3. Otherwise, pick random
        return getEasyMove(availableMoves)
    }

    /**
     * Uses the Minimax algorithm to find the optimal move, ensuring the CPU never loses.
     */
    private fun getHardMove(state: GameState): Int {
        val cpuPlayer = state.currentTurn
        val availableMoves = state.board.indices.filter { state.board[it] == CellState.EMPTY }

        var bestScore = Int.MIN_VALUE
        var bestMove = availableMoves.first()

        // Test each possible move and evaluate its outcome using minimax
        for (move in availableMoves) {
            val testBoard = state.board.toMutableList().apply {
                this[move] = if (cpuPlayer == Player.X) CellState.X else CellState.O
            }
            // CPU is 'maximizing', so the next turn in minimax starts as the opponent (minimizing)
            val score = minimax(testBoard, 0, false, cpuPlayer)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }

        return bestMove
    }

    /**
     * A recursive implementation of the Minimax algorithm.
     * 
     * It explores all possible future moves to determine the optimal move for the current player.
     * It assigns scores to terminal states (win/loss/draw) and propagates them back up the tree.
     * 
     * @param board The simulated board state.
     * @param depth The current recursion depth (used to prioritize faster wins).
     * @param isMaximizing True if evaluating for the CPU, false if for the human opponent.
     * @param cpuPlayer Which [Player] the CPU is playing as.
     * @return The best score achievable from this state.
     */
    private fun minimax(board: List<CellState>, depth: Int, isMaximizing: Boolean, cpuPlayer: Player): Int {
        val winLine = GameEngine.checkWin(board)
        val winner = if (winLine != null) board[winLine[0]] else CellState.EMPTY

        val cpuCellState = if (cpuPlayer == Player.X) CellState.X else CellState.O
        val humanCellState = if (cpuPlayer == Player.X) CellState.O else CellState.X

        // Base cases: Game is over
        if (winner == cpuCellState) return 10 - depth // Reward faster wins
        if (winner == humanCellState) return depth - 10 // Penalize faster losses
        if (board.all { it != CellState.EMPTY }) return 0 // Draw

        val availableMoves = board.indices.filter { board[it] == CellState.EMPTY }

        if (isMaximizing) {
            // CPU's turn: try to maximize the score
            var bestScore = Int.MIN_VALUE
            for (move in availableMoves) {
                val testBoard = board.toMutableList().apply {
                    this[move] = cpuCellState
                }
                val score = minimax(testBoard, depth + 1, false, cpuPlayer)
                bestScore = maxOf(bestScore, score)
            }
            return bestScore
        } else {
            // Human's turn: assume they play optimally to minimize CPU's score
            var bestScore = Int.MAX_VALUE
            for (move in availableMoves) {
                val testBoard = board.toMutableList().apply {
                    this[move] = humanCellState
                }
                val score = minimax(testBoard, depth + 1, true, cpuPlayer)
                bestScore = minOf(bestScore, score)
            }
            return bestScore
        }
    }

    /**
     * Checks if the specified player can win in exactly one move.
     * 
     * @return The winning move index, or null if no immediate win is possible.
     */
    private fun findWinningMove(board: List<CellState>, player: Player): Int? {
        val playerCellState = when (player) {
            Player.X -> CellState.X
            Player.O -> CellState.O
            else -> return null
        }

        board.indices.forEach { index ->
            if (board[index] == CellState.EMPTY) {
                val testBoard = board.toMutableList().apply {
                    this[index] = playerCellState
                }
                if (GameEngine.checkWin(testBoard) != null) {
                    return index
                }
            }
        }
        return null
    }
}
