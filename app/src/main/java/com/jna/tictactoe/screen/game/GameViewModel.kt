package com.jna.tictactoe.screen.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jna.tictactoe.game.CpuPlayer
import com.jna.tictactoe.game.GameEngine
import com.jna.tictactoe.game.model.*
import com.jna.tictactoe.navigation.Game
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel for the Game screen.
 * Implements UDF patterns to manage the Tic-Tac-Toe game state,
 * session scores, and AI opponent logic.
 */
class GameViewModel(
    savedStateHandle: SavedStateHandle? = null
) : ViewModel() {

    // Retrieve navigation arguments (GameMode and Difficulty)
    private var args: Game = try {
        savedStateHandle?.toRoute<Game>() ?: Game(mode = GameMode.VS_HUMAN_LOCAL)
    } catch (e: Exception) {
        // Fallback for tests or unexpected navigation state
        val modeStr = savedStateHandle?.get<String>("mode")
        val diffStr = savedStateHandle?.get<String>("difficulty")
        Game(
            mode = modeStr?.let { GameMode.valueOf(it) } ?: GameMode.VS_CPU,
            difficulty = diffStr?.let { Difficulty.valueOf(it) }
        )
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    /**
     * Initializes a new game with the given mode and difficulty.
     * This resets the board but preserves the session score.
     */
    fun initGame(game: Game) {
        args = game
        resetGame()
    }

    /**
     * Handles a cell being clicked by a player.
     * 
     * @param index The index of the cell clicked (0-8).
     */
    fun onCellClicked(index: Int) {
        val currentState = _uiState.value
        
        // Ignore clicks if the game is over, it's not the player's turn, or cell is occupied
        if (currentState.gameState.phase != GamePhase.PLAYING || 
            currentState.isThinking ||
            !GameEngine.isValidMove(currentState.gameState, index)) {
            return
        }

        applyMove(index)
    }

    /**
     * Resets the game board for a new match while preserving the session score.
     */
    fun resetGame() {
        _uiState.update { 
            it.copy(
                gameState = GameState(mode = args.mode),
                isThinking = false
            )
        }
        
        // If it's VS_CPU and it's O's turn (CPU), trigger AI move
        // This handles cases where the first player is O (not applicable currently but good practice)
        if (args.mode == GameMode.VS_CPU && 
            _uiState.value.gameState.currentTurn == Player.O) {
            triggerAiMove()
        }
    }

    /**
     * Internal helper to apply a move and trigger AI if necessary.
     */
    private fun applyMove(index: Int) {
        val newState = GameEngine.applyMove(_uiState.value.gameState, index)
        updateStateAfterMove(newState)

        // If it's VS_CPU mode and now it's CPU's turn, trigger AI
        if (args.mode == GameMode.VS_CPU && 
            newState.phase == GamePhase.PLAYING && 
            newState.currentTurn == Player.O) {
            triggerAiMove()
        }
    }

    /**
     * Updates the UI state with the new game engine state and increments scores on game end.
     */
    private fun updateStateAfterMove(newGameState: GameState) {
        _uiState.update { currentState ->
            var updatedXWins = currentState.xWins
            var updatedOWins = currentState.oWins
            var updatedDraws = currentState.draws

            if (newGameState.phase == GamePhase.WIN) {
                // If it's WIN, currentTurn is the winner in GameEngine.applyMove
                if (newGameState.currentTurn == Player.X) updatedXWins++
                else if (newGameState.currentTurn == Player.O) updatedOWins++
            } else if (newGameState.phase == GamePhase.DRAW) {
                updatedDraws++
            }

            currentState.copy(
                gameState = newGameState,
                xWins = updatedXWins,
                oWins = updatedOWins,
                draws = updatedDraws
            )
        }
    }

    /**
     * Triggers the AI logic with a natural-feeling delay.
     */
    private fun triggerAiMove() {
        viewModelScope.launch {
            _uiState.update { it.copy(isThinking = true) }
            
            // AI "thinking" delay (600ms - 800ms)
            delay(Random.nextLong(600, 801))
            
            val move = CpuPlayer.getMove(_uiState.value.gameState, args.difficulty ?: Difficulty.MEDIUM)
            if (move != null) {
                val newState = GameEngine.applyMove(_uiState.value.gameState, move)
                updateStateAfterMove(newState)
            }
            
            _uiState.update { it.copy(isThinking = false) }
        }
    }
}
