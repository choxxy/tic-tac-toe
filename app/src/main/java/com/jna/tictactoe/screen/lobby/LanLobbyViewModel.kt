package com.jna.tictactoe.screen.lobby

import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jna.tictactoe.data.PreferenceRepository
import com.jna.tictactoe.network.discovery.NsdDiscoveryManager
import com.jna.tictactoe.network.discovery.NsdRegistrationEvent
import com.jna.tictactoe.network.model.GameMessage
import com.jna.tictactoe.network.socket.GameSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "LanLobbyViewModel"

data class LanLobbyUiState(
    val playerName: String = "Player 1",
    val isHosting: Boolean = false,
    val selectedTab: Int = 0, // 0: Host, 1: Join
    val discoveredHosts: List<NsdServiceInfo> = emptyList(),
    val isConnecting: Boolean = false,
    val error: String? = null,
    val isWaitingForOpponent: Boolean = false
)

sealed class LanLobbyEvent {
    data class GameStarted(val peerName: String, val isHost: Boolean) : LanLobbyEvent()
}

@HiltViewModel
class LanLobbyViewModel @Inject constructor(
    private val nsdDiscoveryManager: NsdDiscoveryManager,
    val gameSocketManager: GameSocketManager,
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LanLobbyUiState())
    val uiState: StateFlow<LanLobbyUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<LanLobbyEvent>(Channel.BUFFERED)
    val events: Flow<LanLobbyEvent> = _eventChannel.receiveAsFlow()

    private var hostingJob: Job? = null
    private var discoveryJob: Job? = null
    private var messageJob: Job? = null
    private var gameStarted = false

    init {
        viewModelScope.launch {
            val name = preferenceRepository.userPreferencesFlow.first().name
            _uiState.update { it.copy(playerName = name) }
        }
        startDiscovery()
        observeMessages()
    }

    fun onPlayerNameChange(newName: String) {
        _uiState.update { it.copy(playerName = newName) }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        if (index == 0) {
            stopDiscovery()
        } else {
            stopHosting()
            startDiscovery()
        }
    }

    fun startHosting() {
        if (_uiState.value.isHosting) return

        stopDiscovery()
        _uiState.update { it.copy(isHosting = true, isWaitingForOpponent = true, error = null) }
        
        hostingJob = viewModelScope.launch {
            try {
                gameSocketManager.host { port ->
                    viewModelScope.launch {
                        nsdDiscoveryManager.registerService(_uiState.value.playerName, port).collect { event ->
                            when (event) {
                                is NsdRegistrationEvent.Registered -> {
                                    Log.d(TAG, "Service registered successfully")
                                }
                                is NsdRegistrationEvent.RegistrationFailed -> {
                                    _uiState.update { it.copy(error = "Failed to register service: ${event.errorCode}") }
                                    stopHosting()
                                }
                                else -> {}
                            }
                        }
                    }
                }
                // Connection established!
                Log.d(TAG, "Opponent connected!")
                if (!performHandshake(true)) {
                    throw java.io.IOException("Handshake failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error hosting: ${e.message}")
                _uiState.update { it.copy(error = "Hosting error: ${e.message}", isHosting = false, isWaitingForOpponent = false) }
            }
        }
    }

    fun stopHosting() {
        hostingJob?.cancel()
        hostingJob = null
        gameSocketManager.disconnect()
        _uiState.update { it.copy(isHosting = false, isWaitingForOpponent = false) }
    }

    private fun startDiscovery() {
        if (discoveryJob != null) return
        discoveryJob = viewModelScope.launch {
            nsdDiscoveryManager.discoverServices().collect { hosts ->
                _uiState.update { it.copy(discoveredHosts = hosts) }
            }
        }
    }

    private fun stopDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = null
    }

    fun connectToHost(host: NsdServiceInfo) {
        _uiState.update { it.copy(isConnecting = true, error = null) }
        viewModelScope.launch {
            try {
                gameSocketManager.connect(host.host.hostAddress ?: "localhost", host.port)
                Log.d(TAG, "Connected to host!")
                if (!performHandshake(false)) {
                    throw java.io.IOException("Handshake failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting: ${e.message}")
                _uiState.update { it.copy(error = "Connection failed: ${e.message}", isConnecting = false) }
            }
        }
    }

    private fun observeMessages() {
        messageJob = viewModelScope.launch {
            gameSocketManager.incomingMessages.collect { message ->
                if (message is GameMessage.Handshake) {
                    Log.d(TAG, "Received handshake from ${message.playerName}")
                    gameStarted = true
                    _eventChannel.send(LanLobbyEvent.GameStarted(message.playerName, _uiState.value.selectedTab == 0))
                }
            }
        }
    }

    private suspend fun performHandshake(isHost: Boolean): Boolean {
        return gameSocketManager.send(GameMessage.Handshake(_uiState.value.playerName))
    }

    override fun onCleared() {
        super.onCleared()
        if (!gameStarted) stopHosting()
        stopDiscovery()
        messageJob?.cancel()
        _eventChannel.close()
    }
}
