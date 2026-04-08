package com.jna.tictactoe.screen.game

import androidx.lifecycle.SavedStateHandle
import com.jna.tictactoe.audio.SoundManager
import com.jna.tictactoe.data.PreferenceRepository
import com.jna.tictactoe.data.UserPreferences
import com.jna.tictactoe.game.model.*
import com.jna.tictactoe.network.socket.GameSocketManager
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelAudioTest {

    @Mock
    private lateinit var soundManager: SoundManager

    @Mock
    private lateinit var socketManager: GameSocketManager

    @Mock
    private lateinit var preferenceRepository: PreferenceRepository

    private lateinit var viewModel: GameViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        `when`(preferenceRepository.userPreferencesFlow).thenReturn(flowOf(UserPreferences()))

        viewModel = GameViewModel(
            savedStateHandle = SavedStateHandle(),
            socketManager = socketManager,
            soundManager = soundManager,
            preferenceRepository = preferenceRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when onCellClicked is called with valid move, playPlacePiece is called`() = runTest {
        // Given a fresh game
        
        // When
        viewModel.onCellClicked(0)
        
        // Then
        verify(soundManager).playPlacePiece()
    }

    @Test
    fun `when a winning move is made, playWin is called`() = runTest {
        // Given a board where X is about to win
        // X X .
        // O O .
        // . . .
        // (This is a bit complex to set up via viewModel directly without exposing state, 
        // but we can simulate moves)
        
        viewModel.onCellClicked(0) // X at 0
        viewModel.onCellClicked(3) // O at 3 (Local mode)
        viewModel.onCellClicked(1) // X at 1
        viewModel.onCellClicked(4) // O at 4
        
        reset(soundManager) // Reset to catch the win sound
        
        viewModel.onCellClicked(2) // X at 2 -> WIN
        
        verify(soundManager).playPlacePiece()
        verify(soundManager).playWin()
    }

    @Test
    fun `when a draw move is made, playDraw is called`() = runTest {
        // Setup a draw scenario
        // X O X
        // X O O
        // O X .
        
        val moves = listOf(0, 1, 2, 4, 3, 5, 7, 6)
        moves.forEach { viewModel.onCellClicked(it) }
        
        reset(soundManager)
        
        viewModel.onCellClicked(8) // Last move -> DRAW
        
        verify(soundManager).playPlacePiece()
        verify(soundManager).playDraw()
    }
}
