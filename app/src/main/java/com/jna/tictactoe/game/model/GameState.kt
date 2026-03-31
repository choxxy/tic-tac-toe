package com.jna.tictactoe.game.model

/**
 * Represents the complete state of a Tic-Tac-Toe game at any given moment.
 * 
 * @property board A list of 9 [CellState] representing the 3x3 grid.
 * @property currentTurn The [Player] whose turn it is to move.
 * @property phase The current [GamePhase] (PLAYING, WIN, DRAW).
 * @property mode The selected [GameMode] (VS CPU, Local, etc.).
 * @property winLine The list of board indices forming the winning combination, or null if no winner.
 */
data class GameState(
    val board: List<CellState> = List(9) { CellState.EMPTY },
    val currentTurn: Player = Player.X,
    val phase: GamePhase = GamePhase.PLAYING,
    val mode: GameMode = GameMode.VS_HUMAN_LOCAL,
    val winLine: List<Int>? = null
)
