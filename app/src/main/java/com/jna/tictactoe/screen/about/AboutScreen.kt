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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.jna.tictactoe.ui.theme.LocalAppDimensions

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    windowSizeClass: WindowSizeClass? = null
) {
    val context = LocalContext.current
    val dimensions = LocalAppDimensions.current
    val isExpanded = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Expanded
    
    val versionName = remember {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
    }
    val copyrightYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    val tutorials = listOf(
        "OBJECTIVE" to "Be the first player to get 3 of your marks in a row — horizontally, vertically, or diagonally.",
        "TAKING TURNS" to "Players alternate turns. On your turn, tap any empty cell on the 3×3 grid to place your mark (X or O). X always goes first.",
        "WINNING" to "A player wins by filling any of these lines with their mark:\n\n• Any row\n• Any column\n• Either diagonal",
        "DRAW" to "If all 9 cells are filled and no player has 3 in a row, the game ends in a draw.",
        "GAME MODES" to "• Pass & Play — local friends.\n• vs AI — challenge the computer.\n• Local Wi-Fi — LAN matchmaking.",
        "TIPS" to "• Control the center.\n• Watch the corners.\n• Block your opponent."
    )

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
                .padding(horizontal = dimensions.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "App Icon",
                modifier = Modifier.size(dimensions.iconSizeLarge + 8.dp)
            )

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

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

            Spacer(modifier = Modifier.height(dimensions.verticalPadding))

            if (isExpanded) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(dimensions.cardSpacing),
                    verticalArrangement = Arrangement.spacedBy(dimensions.cardSpacing)
                ) {
                    items(tutorials) { (title, content) ->
                        TutorialSection(title = title, content = content)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    tutorials.forEach { (title, content) ->
                        TutorialSection(title = title, content = content)
                        Spacer(modifier = Modifier.height(dimensions.cardSpacing))
                    }
                    
                    Text(
                        text = "© $copyrightYear JNA Games",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZenithOutline,
                        modifier = Modifier.padding(vertical = dimensions.verticalPadding)
                    )
                }
            }

            if (isExpanded) {
                Text(
                    text = "© $copyrightYear JNA Games",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZenithOutline,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            BannerAd(
                modifier = Modifier.padding(top = 8.dp, bottom = dimensions.verticalPadding),
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
