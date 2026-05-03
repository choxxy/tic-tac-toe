package com.jna.tictactoe.screen.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jna.tictactoe.R
import com.jna.tictactoe.ui.component.BannerAd
import com.jna.tictactoe.ui.theme.TictactoeTheme
import com.jna.tictactoe.ui.theme.ZenithOnBackground
import com.jna.tictactoe.ui.theme.ZenithOnSurfaceVariant
import com.jna.tictactoe.ui.theme.ZenithOutline
import com.jna.tictactoe.ui.theme.ZenithPrimary
import com.jna.tictactoe.ui.theme.ZenithSurface
import com.jna.tictactoe.ui.theme.ZenithSurfaceContainerLow
import java.util.Calendar

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
    }
    val copyrightYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(
                        "HOW TO PLAY",
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
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "App Icon",
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tic-Tac-Toe",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = ZenithOnBackground
            )

            Text(
                text = "v$versionName",
                style = MaterialTheme.typography.bodyMedium,
                color = ZenithOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            TutorialSection(
                title = "OBJECTIVE",
                content = "Be the first player to get 3 of your marks in a row — horizontally, vertically, or diagonally."
            )

            Spacer(modifier = Modifier.height(32.dp))

            TutorialSection(
                title = "TAKING TURNS",
                content = "Players alternate turns. On your turn, tap any empty cell on the 3×3 grid to place your mark (X or O). X always goes first."
            )

            Spacer(modifier = Modifier.height(32.dp))

            TutorialSection(
                title = "WINNING",
                content = "A player wins by filling any of these lines with their mark:\n\n• Any row (top, middle, or bottom)\n• Any column (left, center, or right)\n• Either diagonal"
            )

            Spacer(modifier = Modifier.height(32.dp))

            TutorialSection(
                title = "DRAW",
                content = "If all 9 cells are filled and no player has 3 in a row, the game ends in a draw."
            )

            Spacer(modifier = Modifier.height(32.dp))

            TutorialSection(
                title = "GAME MODES",
                content = "• Pass & Play — take turns on the same device with a friend or family member.\n\n• vs AI — challenge the computer. Choose Easy, Medium, or Hard difficulty.\n\n• Local Wi-Fi — play against a friend on the same network using LAN matchmaking."
            )

            Spacer(modifier = Modifier.height(32.dp))

            TutorialSection(
                title = "TIPS",
                content = "• Control the center — the center cell gives you the most winning lines.\n\n• Watch the corners — corners are the next most powerful positions.\n\n• Block your opponent — if they have two in a row, place your mark in the third cell to block."
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "© $copyrightYear JNA Games",
                style = MaterialTheme.typography.labelSmall,
                color = ZenithOutline
            )

            Spacer(modifier = Modifier.weight(1f))

            BannerAd(
                modifier = Modifier.padding(top = 8.dp),
                "ca-app-pub-6424626033677167/1624976429"
            )
        }

    }
}

@Composable
private fun TutorialSection(title: String, content: String) {
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

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AboutScreenPreview() {
    TictactoeTheme {
        AboutScreen(onBack = {})
    }
}
