package com.jna.tictactoe.screen.lobby

import android.net.nsd.NsdServiceInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jna.tictactoe.ui.theme.*

/**
 * Screen for Wi-Fi / LAN game discovery and hosting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanLobbyScreen(
    viewModel: LanLobbyViewModel,
    onBack: () -> Unit,
    onGameStarted: (String, Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(event) {
        if (event is LanLobbyEvent.GameStarted) {
            val gameStartedEvent = event as LanLobbyEvent.GameStarted
            onGameStarted(gameStartedEvent.peerName, gameStartedEvent.isHost)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "WI-FI / LAN LOBBY", 
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.sp
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ZenithSurface,
                    titleContentColor = ZenithOnBackground,
                    navigationIconContentColor = ZenithOnBackground
                )
            )
        },
        containerColor = ZenithSurface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Player Name Input
            OutlinedTextField(
                value = uiState.playerName,
                onValueChange = { viewModel.onPlayerNameChange(it) },
                label = { Text("Your Player Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZenithPrimary,
                    unfocusedBorderColor = ZenithSurfaceContainerHigh,
                    focusedLabelColor = ZenithPrimary,
                    unfocusedLabelColor = ZenithOnSurfaceVariant,
                    cursorColor = ZenithPrimary,
                    focusedContainerColor = ZenithSurfaceContainerLow,
                    unfocusedContainerColor = ZenithSurfaceContainerLow
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = ZenithSurfaceContainerLow,
                contentColor = ZenithPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = ZenithPrimary
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { 
                        Text(
                            "Host Game", 
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        ) 
                    }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { 
                        Text(
                            "Join Game", 
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        ) 
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Content based on tab
            Box(modifier = Modifier.weight(1f)) {
                when (uiState.selectedTab) {
                    0 -> HostContent(
                        isHosting = uiState.isHosting,
                        isWaiting = uiState.isWaitingForOpponent,
                        onHostClick = { 
                            if (uiState.isHosting) viewModel.stopHosting() else viewModel.startHosting() 
                        }
                    )
                    1 -> JoinContent(
                        hosts = uiState.discoveredHosts,
                        isConnecting = uiState.isConnecting,
                        onJoinClick = { viewModel.connectToHost(it) }
                    )
                }
            }

            uiState.error?.let {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Text(
                        it, 
                        color = MaterialTheme.colorScheme.onErrorContainer, 
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HostContent(
    isHosting: Boolean,
    isWaiting: Boolean,
    onHostClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isWaiting) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(ZenithSurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ZenithPrimary,
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Waiting for opponent...",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = ZenithOnBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your game is now visible to others\non the same Wi-Fi network.",
                style = MaterialTheme.typography.bodyMedium,
                color = ZenithOnSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(ZenithSurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Cast,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = ZenithOnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Host a New Game",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = ZenithOnBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Create a lobby and wait for a peer\nto join your match.",
                style = MaterialTheme.typography.bodyMedium,
                color = ZenithOnSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onHostClick,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isHosting) ZenithSurfaceContainerHigh else ZenithPrimary,
                contentColor = if (isHosting) ZenithOnBackground else ZenithOnPrimaryContainer
            )
        ) {
            Text(
                if (isHosting) "STOP HOSTING" else "START HOSTING", 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun JoinContent(
    hosts: List<NsdServiceInfo>,
    isConnecting: Boolean,
    onJoinClick: (NsdServiceInfo) -> Unit
) {
    if (hosts.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = ZenithOnSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Searching for games...",
                style = MaterialTheme.typography.titleMedium,
                color = ZenithOnSurfaceVariant
            )
            Text(
                "Make sure you are on the same Wi-Fi.",
                style = MaterialTheme.typography.bodySmall,
                color = ZenithOnSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    "AVAILABLE HOSTS (${hosts.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp
                    ),
                    color = ZenithOnSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            items(hosts) { host ->
                HostItem(host, isConnecting, onClick = { onJoinClick(host) })
            }
        }
    }
}

@Composable
private fun HostItem(
    host: NsdServiceInfo,
    isConnecting: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ZenithSurfaceContainerLow)
            .clickable(enabled = !isConnecting, onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(ZenithPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = host.serviceName.take(1).uppercase(),
                color = ZenithPrimary,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = host.serviceName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ZenithOnBackground
            )
            Text(
                text = "Tap to join match",
                style = MaterialTheme.typography.bodySmall,
                color = ZenithOnSurfaceVariant
            )
        }
        if (isConnecting) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp), 
                strokeWidth = 3.dp,
                color = ZenithPrimary
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.ArrowBack, // Using back arrow flipped if needed, but let's just use a simple icon or nothing
                contentDescription = null,
                tint = ZenithOnSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = 180f }
            )
        }
    }
}
