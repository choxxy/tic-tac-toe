package com.jna.tictactoe.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jna.tictactoe.ui.theme.*

/**
 * A non-blocking overlay that appears when a LAN game connection is lost.
 * Provides a countdown and fallback options.
 */
@Composable
fun ReconnectingOverlay(
    countdown: Int,
    onSwitchToCpu: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(28.dp)),
            color = ZenithSurfaceContainerLow,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (countdown > 0) {
                    CircularProgressIndicator(
                        color = ZenithPrimary,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Connection Lost",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = ZenithOnBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Attempting to reconnect in ${countdown}s...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZenithOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Connection Failed",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = ZenithOnBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "We couldn't re-establish the connection. Would you like to finish the match against the CPU?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZenithOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = onSwitchToCpu,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ZenithPrimary)
                    ) {
                        Text("PLAY VS CPU", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenithOnSurfaceVariant)
                    ) {
                        Text("EXIT TO MENU", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
