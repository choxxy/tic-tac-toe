package com.jna.tictactoe.screen.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jna.tictactoe.ui.theme.*

/**
 * About screen showing app information, credits, and tech stack.
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { 
                    Text(
                        "ABOUT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1.sp
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ZenithSurface,
                    titleContentColor = ZenithOnSurfaceVariant,
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Icon Placeholder
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = ZenithPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = ZenithPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "JNA Tic-Tac-Toe",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = ZenithOnBackground
            )

            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = ZenithOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            AboutSection(
                title = "DESCRIPTION",
                content = "A premium, minimalist Tic-Tac-Toe experience featuring local multiplayer, AI challenges, and LAN-based matchmaking using Network Service Discovery (NSD)."
            )

            Spacer(modifier = Modifier.height(32.dp))

            AboutSection(
                title = "DEVELOPER",
                content = "Built by the JNA team as a demonstration of modern Android development with Jetpack Compose."
            )

            Spacer(modifier = Modifier.height(32.dp))

            AboutSection(
                title = "TECH STACK",
                content = "• Kotlin & Jetpack Compose\n• Material 3 Design\n• NSD for Local Matchmaking\n• StateFlow & ViewModel architecture"
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "© 2024 JNA Systems",
                style = MaterialTheme.typography.labelSmall,
                color = ZenithOutline
            )
        }
    }
}

@Composable
private fun AboutSection(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.sp
            ),
            color = ZenithPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = ZenithSurfaceContainerLow
        ) {
            Text(
                text = content,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = ZenithOnBackground,
                lineHeight = 20.sp
            )
        }
    }
}
