package com.jna.tictactoe.screen.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jna.tictactoe.game.model.*
import com.jna.tictactoe.ui.theme.*

/**
 * The Game screen where the actual match takes place.
 * Adheres to the "Architectural Serenity" design system with a no-line grid.
 *
 * @param onExit Callback to navigate back to the main menu.
 * @param viewModel The ViewModel for this screen, scoped for session persistence.
 */
@Composable
fun GameScreen(
    onExit: () -> Unit,
    viewModel: GameViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

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
            // Top Bar
            GameTopBar(onExit = onExit)

            Spacer(modifier = Modifier.height(40.dp))

            // Score Board
            ScoreBoard(
                xWins = uiState.xWins,
                oWins = uiState.oWins,
                draws = uiState.draws
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Turn Indicator
            TurnIndicator(
                currentTurn = uiState.gameState.currentTurn,
                isThinking = uiState.isThinking
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Game Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ZenithSurfaceContainerHigh)
                    .padding(12.dp)
            ) {
                GameBoard(
                    board = uiState.gameState.board,
                    winLine = uiState.gameState.winLine,
                    onCellClicked = viewModel::onCellClicked
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Actions (Optional, could be Quit/Reset if not in dialog)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = viewModel::resetGame) {
                    Text(
                        text = "RESET BOARD",
                        style = MaterialTheme.typography.titleSmall,
                        color = ZenithOnSurfaceVariant
                    )
                }
            }
        }

        // Result Dialog
        if (uiState.gameState.phase != GamePhase.PLAYING) {
            ResultDialog(
                phase = uiState.gameState.phase,
                winner = if (uiState.gameState.phase == GamePhase.WIN) uiState.gameState.currentTurn else null,
                onPlayAgain = viewModel::resetGame,
                onNewMatch = {
                    // In this context, New Match might mean resetting scores or just going home
                    // We'll reset and go home as per the plan.
                    onExit()
                }
            )
        }
    }
}

@Composable
private fun GameTopBar(onExit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onExit) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = ZenithOnBackground
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "MATCH SESSION",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.sp
            ),
            color = ZenithOnSurfaceVariant
        )
    }
}

@Composable
private fun ScoreBoard(xWins: Int, oWins: Int, draws: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ZenithSurfaceContainerLow)
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreItem(label = "PLAYER X", score = xWins, color = ZenithPrimary)
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "DRAWS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = ZenithOnSurfaceVariant
            )
            Text(
                text = draws.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = ZenithOnBackground
            )
        }

        ScoreItem(label = "PLAYER O", score = oWins, color = ZenithSecondary)
    }
}

@Composable
private fun ScoreItem(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = ZenithOnBackground
        )
    }
}

@Composable
private fun TurnIndicator(currentTurn: Player, isThinking: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = ZenithSurfaceContainerLowest,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (currentTurn == Player.X) ZenithPrimary else ZenithSecondary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isThinking) "CPU IS THINKING..." else "${currentTurn}'S TURN",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ZenithOnBackground
                )
            }
        }
    }
}

@Composable
private fun GameBoard(
    board: List<CellState>,
    winLine: List<Int>?,
    onCellClicked: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    val isWinningCell = winLine?.contains(index) == true
                    GameCell(
                        modifier = Modifier.weight(1f),
                        state = board[index],
                        isWinningCell = isWinningCell,
                        onClick = { onCellClicked(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCell(
    modifier: Modifier = Modifier,
    state: CellState,
    isWinningCell: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isWinningCell) {
        if (state == CellState.X) ZenithPrimaryContainer else ZenithSecondaryContainer
    } else {
        ZenithSurfaceContainerLowest
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(enabled = state == CellState.EMPTY, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = state != CellState.EMPTY,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.8f, animationSpec = tween(300))
        ) {
            when (state) {
                CellState.X -> PlayerXIcon()
                CellState.O -> PlayerOIcon()
                CellState.EMPTY -> {}
            }
        }
    }
}

@Composable
private fun PlayerXIcon() {
    val xGradient = Brush.verticalGradient(
        colors = listOf(ZenithPrimary, ZenithPrimaryDim)
    )
    
    // Draw a custom X with a gradient
    androidx.compose.foundation.Canvas(modifier = Modifier.size(48.dp)) {
        val strokeWidth = 12.dp.toPx()
        val size = this.size.width
        val padding = 4.dp.toPx()
        
        // Main diagonal (\)
        drawLine(
            brush = xGradient,
            start = androidx.compose.ui.geometry.Offset(padding, padding),
            end = androidx.compose.ui.geometry.Offset(size - padding, size - padding),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        // Anti-diagonal (/)
        drawLine(
            brush = xGradient,
            start = androidx.compose.ui.geometry.Offset(size - padding, padding),
            end = androidx.compose.ui.geometry.Offset(padding, size - padding),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
private fun PlayerOIcon() {
    val oGradient = Brush.verticalGradient(
        colors = listOf(ZenithSecondary, ZenithSecondaryDim)
    )
    
    // Draw a custom O with a gradient
    androidx.compose.foundation.Canvas(modifier = Modifier.size(44.dp)) {
        val strokeWidth = 12.dp.toPx()
        val size = this.size.width
        val padding = 4.dp.toPx()
        
        drawCircle(
            brush = oGradient,
            radius = (size / 2) - padding,
            center = androidx.compose.ui.geometry.Offset(size / 2, size / 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

@Composable
private fun ResultDialog(
    phase: GamePhase,
    winner: Player?,
    onPlayAgain: () -> Unit,
    onNewMatch: () -> Unit
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ZenithSurfaceContainerLowest.copy(alpha = 0.7f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                color = ZenithSurfaceContainerLowest,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (phase) {
                            GamePhase.WIN -> "VICTORY"
                            GamePhase.DRAW -> "DRAW"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.2.sp
                        ),
                        color = ZenithOnSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when (phase) {
                            GamePhase.WIN -> "Player $winner Wins"
                            GamePhase.DRAW -> "No Winner"
                            else -> ""
                        },
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = ZenithOnBackground
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZenithSurfaceContainerHighest,
                            contentColor = ZenithPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(
                            text = "PLAY AGAIN",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = onNewMatch,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "QUIT TO MENU",
                            style = MaterialTheme.typography.titleSmall,
                            color = ZenithOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
