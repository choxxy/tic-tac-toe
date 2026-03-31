package com.jna.tictactoe.screen.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jna.tictactoe.ui.theme.TictactoeTheme
import com.jna.tictactoe.ui.theme.ZenithOnBackground
import com.jna.tictactoe.ui.theme.ZenithOnPrimaryContainer
import com.jna.tictactoe.ui.theme.ZenithOnSurfaceVariant
import com.jna.tictactoe.ui.theme.ZenithPrimary
import com.jna.tictactoe.ui.theme.ZenithPrimaryContainer
import com.jna.tictactoe.ui.theme.ZenithSurface
import com.jna.tictactoe.ui.theme.ZenithSurfaceContainerHigh
import com.jna.tictactoe.ui.theme.ZenithSurfaceContainerLow
import com.jna.tictactoe.ui.theme.ZenithSurfaceContainerLowest

/**
 * The primary entry point for the application, displaying game mode options and player profile.
 * 
 * @param onVsCpu Callback when "Play vs CPU" is selected.
 * @param onVsLocal Callback when "Local" (Pass and Play) is selected.
 * @param onVsLan Callback when "Wi-Fi / LAN" is selected.
 */
@Composable
fun MainMenuScreen(
    onVsCpu: () -> Unit,
    onVsLocal: () -> Unit,
    onVsLan: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenithSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Top Navigation Bar
            TopNavigationBar()

            Spacer(modifier = Modifier.height(56.dp))

            // Current Season Header
            Column {
                Text(
                    text = "CURRENT SEASON",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp
                    ),
                    color = ZenithOnSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "The Grand Master\nSeries",
                    style = MaterialTheme.typography.displayMedium.copy(
                        lineHeight = 44.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = ZenithOnBackground
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Primary Card: VS CPU
            GameModeCardLarge(
                title = "Play vs CPU",
                subtitle = "Challenge our adaptive neural engine.",
                onClick = onVsCpu
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Cards: Local & LAN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GameModeCardSmall(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Group,
                    title = "Local",
                    subtitle = "Two players, one\ndevice.",
                    onClick = onVsLocal
                )
                GameModeCardSmall(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Public,
                    title = "Wi-Fi / LAN",
                    subtitle = "Play over local\nnetwork.",
                    onClick = onVsLan
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Profile Card
            ProfileCard(
                name = "Alex Grayson",
                rank = "RANKED AMATEUR",
                winRate = "64%"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Displays the top app bar with app title and navigation actions.
 */
@Composable
private fun TopNavigationBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Default.GridView,
            contentDescription = "Menu",
            tint = ZenithOnBackground,
            modifier = Modifier
                .size(28.dp)
                .clickable { }
        )

        Text(
            text = "TIC TAC TOE",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.05.sp
            ),
            color = ZenithOnBackground
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "History",
                tint = ZenithOnBackground,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { }
            )
            Spacer(modifier = Modifier.width(20.dp))
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = ZenithOnBackground,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { }
            )
        }
    }
}

@Composable
private fun GameModeCardLarge(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ZenithSurfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ZenithPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ElectricBolt,
                        contentDescription = null,
                        tint = ZenithPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = ZenithOnBackground
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZenithOnSurfaceVariant
                )
            }

            // Robot Icon Placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Approximate the robot head from the design
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp, 16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(ZenithSurfaceContainerHigh)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp, 16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(ZenithSurfaceContainerHigh)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(64.dp, 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ZenithSurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(ZenithSurfaceContainerLowest)
                            )
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(ZenithSurfaceContainerLowest)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameModeCardSmall(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(ZenithSurfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ZenithSurfaceContainerHigh.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ZenithOnBackground,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ZenithOnBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
            color = ZenithOnSurfaceVariant
        )
    }
}

@Composable
private fun ProfileCard(
    name: String,
    rank: String,
    winRate: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ZenithSurfaceContainerLow)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Placeholder
            Box(
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ZenithSurfaceContainerHigh)
                ) {
                    // Simple placeholder for avatar
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        tint = ZenithOnSurfaceVariant
                    )
                }
                // Online indicator
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(ZenithSurfaceContainerLowest)
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(ZenithPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rank,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.05.sp
                    ),
                    color = ZenithOnSurfaceVariant
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ZenithOnBackground
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "WIN RATE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ZenithOnSurfaceVariant
                )
                Text(
                    text = winRate,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = ZenithOnPrimaryContainer
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun MainMenuScreenPreview() {
    TictactoeTheme {
        MainMenuScreen(onVsCpu = {}, onVsLocal = {}, onVsLan = {})
    }
}
