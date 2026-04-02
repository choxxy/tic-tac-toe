package com.jna.tictactoe.screen.lobby

import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jna.tictactoe.network.discovery.NsdDiscoveryManager
import com.jna.tictactoe.network.discovery.NsdRegistrationEvent
import com.jna.tictactoe.network.model.GameMessage
import com.jna.tictactoe.network.socket.GameSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "LanLobbyViewModel"

data class LanLobbyUiState(
    val playerName: String = "Player ${ (1000..9999).random() }",
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
    val gameSocketManager: GameSocketManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(LanLobbyUiState())
    val uiState: StateFlow<LanLobbyUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LanLobbyEvent>()
    val events: SharedFlow<LanLobbyEvent> = _events.asSharedFlow()

    private var hostingJob: Job? = null
    private var discoveryJob: Job? = null
    private var messageJob: Job? = null

    init {
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
                performHandshake(true)
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
                performHandshake(false)
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
                    _events.emit(LanLobbyEvent.GameStarted(message.playerName, _uiState.value.selectedTab == 0))
                }
            }
        }
    }

    private suspend fun performHandshake(isHost: Boolean) {
        gameSocketManager.send(GameMessage.Handshake(_uiState.value.playerName))
    }

    override fun onCleared() {
        super.onCleared()
        stopHosting()
        stopDiscovery()
        messageJob?.cancel()
    }
}
