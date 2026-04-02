package com.jna.tictactoe.network.socket

import com.jna.tictactoe.network.model.GameMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class GameSocketManagerTest {

    @Test
    fun testBidirectionalCommunication() = runBlocking {
        val hostManager = GameSocketManager()
        val clientManager = GameSocketManager()
        
        val portDeferred = CompletableDeferred<Int>()
        
        val hostJob = launch(Dispatchers.IO) {
            try {
                hostManager.host { port ->
                    portDeferred.complete(port)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        
        val hostPort = portDeferred.await()
        
        val clientJob = launch(Dispatchers.IO) {
            try {
                clientManager.connect("127.0.0.1", hostPort)
            } catch (e: Exception) {
                // Ignore
            }
        }
        
        // Give time for connection to establish
        delay(2000)
        
        val hostMessage = GameMessage.Handshake("Host")
        val clientMessage = GameMessage.Handshake("Client")
        
        // Start collecting before sending
        val clientReceived = async(Dispatchers.IO) {
            clientManager.incomingMessages.first()
        }
        val hostReceived = async(Dispatchers.IO) {
            hostManager.incomingMessages.first()
        }
        
        // Small delay to ensure collectors are registered
        delay(500)
        
        hostManager.send(hostMessage)
        clientManager.send(clientMessage)
        
        assertEquals(hostMessage, clientReceived.withTimeout(5000))
        assertEquals(clientMessage, hostReceived.withTimeout(5000))

        clientJob.cancelAndJoin()
        hostJob.cancelAndJoin()
    }

    private suspend fun <T> Deferred<T>.withTimeout(timeoutMillis: Long): T {
        return withTimeout(timeoutMillis) { await() }
    }
}
