package com.jna.tictactoe.network.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.IOException

private const val TAG = "NsdDiscoveryManager"
private const val SERVICE_TYPE = "_tictactoe._tcp."

sealed class NsdRegistrationEvent {
    data class Registered(val serviceInfo: NsdServiceInfo) : NsdRegistrationEvent()
    data class RegistrationFailed(val errorCode: Int) : NsdRegistrationEvent()
    data class Unregistered(val serviceInfo: NsdServiceInfo) : NsdRegistrationEvent()
    data class UnregistrationFailed(val errorCode: Int) : NsdRegistrationEvent()
}

class NsdDiscoveryManager(context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    fun registerService(name: String, port: Int): Flow<NsdRegistrationEvent> = callbackFlow {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = name
            serviceType = SERVICE_TYPE
            setPort(port)
        }

        val registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(si: NsdServiceInfo) {
                Log.d(TAG, "Service registered: ${si.serviceName}")
                trySend(NsdRegistrationEvent.Registered(si))
            }

            override fun onRegistrationFailed(si: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Registration failed: $errorCode")
                trySend(NsdRegistrationEvent.RegistrationFailed(errorCode))
                close()
            }

            override fun onServiceUnregistered(si: NsdServiceInfo) {
                Log.d(TAG, "Service unregistered: ${si.serviceName}")
                trySend(NsdRegistrationEvent.Unregistered(si))
                close()
            }

            override fun onUnregistrationFailed(si: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Unregistration failed: $errorCode")
                trySend(NsdRegistrationEvent.UnregistrationFailed(errorCode))
                close()
            }
        }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)

        awaitClose {
            try {
                nsdManager.unregisterService(registrationListener)
            } catch (e: Exception) {
                Log.e(TAG, "Error in awaitClose: ${e.message}")
            }
        }
    }

    fun discoverServices(): Flow<List<NsdServiceInfo>> = callbackFlow {
        val discoveredServices = mutableMapOf<String, NsdServiceInfo>()

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(type: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: $errorCode")
                close(IOException("Discovery failed: $errorCode"))
            }

            override fun onStopDiscoveryFailed(type: String, errorCode: Int) {
                Log.e(TAG, "Stop discovery failed: $errorCode")
                nsdManager.stopServiceDiscovery(this)
                close()
            }

            override fun onDiscoveryStarted(type: String) {
                Log.d(TAG, "Discovery started")
            }

            override fun onDiscoveryStopped(type: String) {
                Log.d(TAG, "Discovery stopped")
                close()
            }

            override fun onServiceFound(si: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${si.serviceName}")
                if (si.serviceType == SERVICE_TYPE) {
                    // Start resolving immediately
                    nsdManager.resolveService(si, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(si: NsdServiceInfo, errorCode: Int) {
                            Log.e(TAG, "Resolve failed: $errorCode for ${si.serviceName}")
                        }

                        override fun onServiceResolved(resolved: NsdServiceInfo) {
                            Log.d(TAG, "Service resolved: ${resolved.serviceName} at ${resolved.host}:${resolved.port}")
                            discoveredServices[resolved.serviceName] = resolved
                            trySend(discoveredServices.values.toList())
                        }
                    })
                }
            }

            override fun onServiceLost(si: NsdServiceInfo) {
                Log.d(TAG, "Service lost: ${si.serviceName}")
                discoveredServices.remove(si.serviceName)
                trySend(discoveredServices.values.toList())
            }
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

        awaitClose {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                Log.e(TAG, "Error in awaitClose: ${e.message}")
            }
        }
    }
}
