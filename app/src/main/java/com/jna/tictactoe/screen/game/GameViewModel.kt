package com.jna.tictactoe.screen.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jna.tictactoe.audio.SoundManager
import com.jna.tictactoe.game.CpuPlayer
import com.jna.tictactoe.game.GameEngine
import com.jna.tictactoe.game.model.*
import com.jna.tictactoe.navigation.Game
import com.jna.tictactoe.network.model.GameMessage
import com.jna.tictactoe.network.socket.GameSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel for the Game screen.
 * Implements UDF patterns to manage the Tic-Tac-Toe game state,
 * session scores, and AI opponent logic.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val socketManager: GameSocketManager,
    private val soundManager: SoundManager
) : ViewModel() {

    // Retrieve navigation arguments (GameMode and Difficulty)
    private var args: Game = try {
        savedStateHandle.toRoute<Game>()
    } catch (e: Exception) {
        // Fallback for tests or unexpected navigation state
        val modeStr = savedStateHandle.get<String>("mode")
        val diffStr = savedStateHandle.get<String>("difficulty")
        val isHost = savedStateHandle.get<Boolean>("isHost") ?: true
        val peerName = savedStateHandle.get<String>("peerName")
        Game(
            mode = modeStr?.let { GameMode.valueOf(it) } ?: GameMode.VS_CPU,
            difficulty = diffStr?.let { Difficulty.valueOf(it) },
            isHost = isHost,
            peerName = peerName
        )
    }

    private val _uiState = MutableStateFlow(
        GameUiState(
            isHost = args.isHost,
            peerName = args.peerName
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var lastMessageReceivedTime = System.currentTimeMillis()
    private var heartbeatJob: Job? = null
    private var reconnectionJob: Job? = null

    init {
        soundManager.loadSounds()
        if (args.mode == GameMode.VS_LAN) {
            observeNetworkMessages()
            startHeartbeat()
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
        socketManager.disconnect()
    }

    /**
     * Sends a heartbeat every 2 seconds and detects disconnects.
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (isActive) {
                delay(2000)
                if (!socketManager.send(GameMessage.Heartbeat)) {
                    handleDisconnect()
                    break
                }

                if (System.currentTimeMillis() - lastMessageReceivedTime > 5000) {
                    handleDisconnect()
                    break
                }
            }
        }
    }

    private fun handleDisconnect() {
        if (_uiState.value.isReconnecting) return
        
        _uiState.update { it.copy(isReconnecting = true, reconnectCountdown = 9) }
        startReconnectionProcess()
    }

    private fun startReconnectionProcess() {
        reconnectionJob?.cancel()
        reconnectionJob = viewModelScope.launch {
            var countdown = 9
            while (countdown > 0) {
                // Try to reconnect
                try {
                    if (args.isHost) {
                        // Host: just wait for a connection
                        viewModelScope.launch {
                            try {
                                socketManager.host { }
                                onReconnected()
                            } catch (e: Exception) { }
                        }
                    } else {
                        // Guest: try to connect to last host
                        val host = socketManager.lastHost
                        val port = socketManager.lastPort
                        if (host != null && port != null) {
                            socketManager.connect(host, port)
                            onReconnected()
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Reconnection failed, wait and try again
                }
                
                delay(1000)
                if (!_uiState.value.isReconnecting) break
                countdown--
                _uiState.update { it.copy(reconnectCountdown = countdown) }
            }
            
            if (countdown == 0 && _uiState.value.isReconnecting) {
                // Timeout
                _uiState.update { it.copy(reconnectCountdown = 0) }
            }
        }
    }

    private fun onReconnected() {
        _uiState.update { it.copy(isReconnecting = false, reconnectCountdown = null) }
        lastMessageReceivedTime = System.currentTimeMillis()
        startHeartbeat()
        // If host, broadcast current state to sync the reconnected guest
        if (args.isHost) {
            broadcastState()
        }
    }

    fun switchToCpuMode() {
        reconnectionJob?.cancel()
        heartbeatJob?.cancel()
        _uiState.update { 
            it.copy(
                isReconnecting = false, 
                reconnectCountdown = null,
                gameState = it.gameState.copy(mode = GameMode.VS_CPU),
                peerName = "CPU (Easy)"
            ) 
        }
        args = args.copy(mode = GameMode.VS_CPU, difficulty = Difficulty.EASY)
        
        if (_uiState.value.gameState.currentTurn == Player.O) {
            triggerAiMove()
        }
    }

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

        if (args.mode == GameMode.VS_LAN) {
            // In LAN mode, check if it's actually our turn
            if (currentState.isWaitingForPeerMove) return

            if (args.isHost) {
                // Host: apply locally and sync
                soundManager.playPlacePiece()
                applyMove(index)
            } else {
                // Guest: send move request to host
                viewModelScope.launch {
                    if (!socketManager.send(GameMessage.Move(index))) {
                        handleDisconnect()
                    }
                }
            }
        } else {
            // Local or CPU mode: apply move directly
            soundManager.playPlacePiece()
            applyMove(index)
        }
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
        
        // If Host in LAN mode, sync the reset state
        if (args.mode == GameMode.VS_LAN && args.isHost) {
            broadcastState()
        }

        // If it's VS_CPU and it's O's turn (CPU), trigger AI move
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

        if (args.mode == GameMode.VS_LAN && args.isHost) {
            broadcastState()
        }

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

            if (newGameState.phase != currentState.gameState.phase) {
                if (newGameState.phase == GamePhase.WIN) {
                    // If it's WIN, currentTurn is the winner in GameEngine.applyMove
                    soundManager.playWin()
                    if (newGameState.currentTurn == Player.X) updatedXWins++
                    else if (newGameState.currentTurn == Player.O) updatedOWins++
                } else if (newGameState.phase == GamePhase.DRAW) {
                    soundManager.playDraw()
                    updatedDraws++
                }
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
     * Observes incoming messages from the network socket.
     */
    private fun observeNetworkMessages() {
        viewModelScope.launch {
            socketManager.incomingMessages.collect { message ->
                lastMessageReceivedTime = System.currentTimeMillis()
                when (message) {
                    is GameMessage.Move -> {
                        if (args.isHost) {
                            // Host: validate and apply guest move
                            if (GameEngine.isValidMove(_uiState.value.gameState, message.index) && 
                                _uiState.value.gameState.currentTurn == Player.O) {
                                soundManager.playPlacePiece()
                                applyMove(message.index)
                            }
                        }
                    }
                    is GameMessage.SyncState -> {
                        if (!args.isHost) {
                            // Guest: replace local state with authoritative host state
                            val currentPhase = _uiState.value.gameState.phase
                            val newPhase = message.state.phase
                            if (newPhase == GamePhase.PLAYING && currentPhase == GamePhase.PLAYING) {
                                soundManager.playPlacePiece()
                            }
                            updateStateAfterMove(message.state)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Broadcasts the current game state to the peer.
     */
    private fun broadcastState() {
        viewModelScope.launch {
            if (!socketManager.send(GameMessage.SyncState(_uiState.value.gameState))) {
                handleDisconnect()
            }
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
                soundManager.playPlacePiece()
                val newState = GameEngine.applyMove(_uiState.value.gameState, move)
                updateStateAfterMove(newState)
            }
            
            _uiState.update { it.copy(isThinking = false) }
        }
    }
}
