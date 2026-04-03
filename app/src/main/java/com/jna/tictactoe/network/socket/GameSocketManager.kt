package com.jna.tictactoe.network.socket

import android.util.Log
import com.jna.tictactoe.network.model.GameMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.io.IOException

private const val TAG = "GameSocketManager"

class GameSocketManager {
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var messageLoopJob: Job? = null

    var lastHost: String? = null
        private set
    var lastPort: Int? = null
        private set
    
    private val _incomingMessages = MutableSharedFlow<GameMessage>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingMessages: SharedFlow<GameMessage> = _incomingMessages.asSharedFlow()

    /**
     * Hosts a game on an auto-assigned port.
     * Invokes [onPortAvailable] when the server socket is ready.
     * Returns as soon as a client connects.
     */
    suspend fun host(onPortAvailable: (Int) -> Unit) = withContext(Dispatchers.IO) {
        disconnect()
        ServerSocket(lastPort ?: 0).use { serverSocket ->
            val port = serverSocket.localPort
            lastPort = port
            lastHost = null // indicate we are host
            onPortAvailable(port)
            
            val clientSocket = serverSocket.accept()
            setupConnection(clientSocket)
        }
    }

    /**
     * Connects to a host.
     * Returns as soon as the connection is established.
     */
    suspend fun connect(host: String, port: Int) = withContext(Dispatchers.IO) {
        disconnect()
        lastHost = host
        lastPort = port
        val clientSocket = Socket(host, port)
        setupConnection(clientSocket)
    }

    private fun setupConnection(s: Socket) {
        socket = s
        writer = PrintWriter(s.getOutputStream(), true)
        messageLoopJob = scope.launch {
            handleConnection(s)
        }
    }

    private suspend fun handleConnection(s: Socket) {
        val reader = BufferedReader(InputStreamReader(withContext(Dispatchers.IO) {
            s.getInputStream()
        }))
        try {
            while (currentCoroutineContext().isActive) {
                val line = try {
                    withContext(Dispatchers.IO) {
                        reader.readLine()
                    }
                } catch (e: IOException) {
                    null
                } ?: break
                
                try {
                    val message = json.decodeFromString<GameMessage>(line)
                    _incomingMessages.emit(message)
                } catch (e: Exception) {
                    Log.e(TAG, "Error decoding message: ${e.message}")
                }
            }
        } catch (e: IOException) {
            if (currentCoroutineContext().isActive) {
                Log.e(TAG, "Connection error: ${e.message}")
            }
        } finally {
            disconnect()
        }
    }

    fun disconnect() {
        messageLoopJob?.cancel()
        messageLoopJob = null
        writer = null
        try {
            socket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing socket: ${e.message}")
        }
        socket = null
    }

    suspend fun send(message: GameMessage): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentWriter = writer ?: return@withContext false
            val jsonString = json.encodeToString(message)
            currentWriter.println(jsonString)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}")
            false
        }
    }
}
