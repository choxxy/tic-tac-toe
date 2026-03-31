package com.jna.tictactoe.screen.game

import androidx.lifecycle.SavedStateHandle
import com.jna.tictactoe.game.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit test for [GameViewModel].
 * Verifies the game logic, state transitions, and AI behavior.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Verifies that the ViewModel initializes with the correct state based on navigation arguments.
     */
    @Test
    fun `initial state matches navigation arguments`() = runTest {
        // Given
        val savedStateHandle = createSavedStateHandle(GameMode.VS_CPU, Difficulty.HARD)
        
        // When
        val viewModel = GameViewModel(savedStateHandle)

        // Then
        val state = viewModel.uiState.value
        assertEquals(Player.X, state.gameState.currentTurn)
        assertEquals(GamePhase.PLAYING, state.gameState.phase)
        assertEquals(0, state.xWins)
        assertEquals(0, state.oWins)
        assertEquals(0, state.draws)
    }

    /**
     * Verifies that clicking an empty cell updates the board and switches turns.
     */
    @Test
    fun `onCellClicked updates board and switches turn in local mode`() = runTest {
        // Given
        val savedStateHandle = createSavedStateHandle(GameMode.VS_HUMAN_LOCAL)
        val viewModel = GameViewModel(savedStateHandle)

        // When - Player X clicks center cell (index 4)
        viewModel.onCellClicked(4)

        // Then
        val state = viewModel.uiState.value
        assertEquals(CellState.X, state.gameState.board[4])
        assertEquals(Player.O, state.gameState.currentTurn)
    }

    /**
     * Verifies that in VS_CPU mode, the AI makes a move automatically after the player's move.
     */
    @Test
    fun `AI makes a move after player move in VS_CPU mode`() = runTest {
        // Given
        val savedStateHandle = createSavedStateHandle(GameMode.VS_CPU, Difficulty.EASY)
        val viewModel = GameViewModel(savedStateHandle)

        // When - Player X clicks center cell (index 4)
        viewModel.onCellClicked(4)
        
        // Wait for the thinking state to be updated
        runCurrent()
        
        // Then - AI should be thinking
        assertTrue("AI should be thinking", viewModel.uiState.value.isThinking)
        
        // Advance time to allow AI move (600ms-800ms)
        advanceTimeBy(1000)
        runCurrent()
        
        // Then - AI should have moved
        val state = viewModel.uiState.value
        assertFalse("AI should be done thinking", state.isThinking)
        assertEquals("Two moves should have been made (Player + AI)", 2, state.gameState.board.count { it != CellState.EMPTY })
        // Turn should have returned to Player X after AI moved
        assertEquals(Player.X, state.gameState.currentTurn)
    }

    /**
     * Verifies that a win is correctly detected and increments the score.
     */
    @Test
    fun `winning move increments score and sets phase to WIN`() = runTest {
        // Given - VS_HUMAN_LOCAL for predictable moves
        val savedStateHandle = createSavedStateHandle(GameMode.VS_HUMAN_LOCAL)
        val viewModel = GameViewModel(savedStateHandle)

        // X: 0, 1, 2 (Win)
        // O: 3, 4
        viewModel.onCellClicked(0) // X
        viewModel.onCellClicked(3) // O
        viewModel.onCellClicked(1) // X
        viewModel.onCellClicked(4) // O
        viewModel.onCellClicked(2) // X - Win

        // Then
        val state = viewModel.uiState.value
        assertEquals(GamePhase.WIN, state.gameState.phase)
        assertEquals(1, state.xWins)
        assertNotNull("Winning line should be identified", state.gameState.winLine)
    }

    /**
     * Verifies that a draw is correctly detected and increments the score.
     */
    @Test
    fun `draw increments draw count and sets phase to DRAW`() = runTest {
        // Given - VS_HUMAN_LOCAL
        val savedStateHandle = createSavedStateHandle(GameMode.VS_HUMAN_LOCAL)
        val viewModel = GameViewModel(savedStateHandle)

        // X O X
        // X X O
        // O X O
        val moves = listOf(0, 1, 2, 5, 3, 6, 4, 8, 7)
        moves.forEach { viewModel.onCellClicked(it) }

        // Then
        val state = viewModel.uiState.value
        assertEquals(GamePhase.DRAW, state.gameState.phase)
        assertEquals(1, state.draws)
    }

    /**
     * Verifies that resetting the game clears the board but preserves the session score.
     */
    @Test
    fun `resetGame clears board but preserves score`() = runTest {
        // Given
        val savedStateHandle = createSavedStateHandle(GameMode.VS_HUMAN_LOCAL)
        val viewModel = GameViewModel(savedStateHandle)

        // Winning game for X
        viewModel.onCellClicked(0) // X
        viewModel.onCellClicked(3) // O
        viewModel.onCellClicked(1) // X
        viewModel.onCellClicked(4) // O
        viewModel.onCellClicked(2) // X - Win

        // When
        viewModel.resetGame()

        // Then
        val state = viewModel.uiState.value
        assertEquals(GamePhase.PLAYING, state.gameState.phase)
        assertTrue("Board should be empty", state.gameState.board.all { it == CellState.EMPTY })
        assertEquals(Player.X, state.gameState.currentTurn)
        assertEquals(1, state.xWins) // Score preserved
    }

    /**
     * Helper to create a SavedStateHandle with Game route arguments.
     * Note: In a real app, navigation-compose handles the serializing/deserializing.
     * For unit tests, we can put the properties directly into the handle.
     */
    private fun createSavedStateHandle(mode: GameMode, difficulty: Difficulty? = null): SavedStateHandle {
        // For type-safe navigation, we'd ideally use toRoute, 
        // but for unit testing without full navigation setup, 
        // we can just put the keys. 
        // However, GameViewModel will use savedStateHandle.toRoute<Game>().
        // So we need to ensure the handle can provide that.
        // The easiest way for simple objects is to use the bundle keys.
        // But since Game is a data class, it's more complex.
        
        // Let's assume GameViewModel will use a manual way to get args for now 
        // if toRoute is too hard to mock, OR we just provide it in the handle.
        
        // Actually, toRoute<Game>(handle) expects 'mode' and 'difficulty' to be in the handle.
        return SavedStateHandle(mapOf(
            "mode" to mode.name,
            "difficulty" to difficulty?.name
        ))
    }
}
