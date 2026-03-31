package com.jna.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.jna.tictactoe.game.model.Difficulty
import com.jna.tictactoe.game.model.GameMode
import com.jna.tictactoe.navigation.Game
import com.jna.tictactoe.navigation.Menu
import com.jna.tictactoe.navigation.Splash
import com.jna.tictactoe.screen.game.GameScreen
import com.jna.tictactoe.screen.game.GameViewModel
import com.jna.tictactoe.screen.menu.MainMenuScreen
import com.jna.tictactoe.screen.splash.SplashScreen
import com.jna.tictactoe.ui.theme.TictactoeTheme

/**
 * The main activity of the application, responsible for initializing the navigation graph
 * and setting up the Jetpack Compose UI.
 */
class MainActivity : ComponentActivity() {
    /**
     * Entry point for the activity, setting up the UI and navigation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up the edge-to-edge display to use the full screen area
        enableEdgeToEdge()
        setContent {
            TictactoeTheme {
                val navController = rememberNavController()
                
                // Shared ViewModel scoped to the Activity for session-long score persistence
                val gameViewModel: GameViewModel = viewModel()

                // Configure the main navigation host with Splash as the initial screen
                NavHost(
                    navController = navController,
                    startDestination = Splash
                ) {
                    // Splash screen shown on app launch
                    composable<Splash> {
                        SplashScreen(
                            onTimeout = {
                                // Navigate to Menu and remove Splash from the backstack
                                navController.navigate(Menu) {
                                    popUpTo(Splash) { inclusive = true }
                                }
                            }
                        )
                    }
                    // Main menu screen with game mode selection
                    composable<Menu> {
                        MainMenuScreen(
                            onVsCpu = {
                                navController.navigate(Game(mode = GameMode.VS_CPU, difficulty = Difficulty.EASY))
                            },
                            onVsLocal = {
                                navController.navigate(Game(mode = GameMode.VS_HUMAN_LOCAL))
                            },
                            onVsLan = { /* TODO: Implement LAN mode navigation */ }
                        )
                    }
                    // Game screen with the actual match
                    composable<Game> { backStackEntry ->
                        val args = backStackEntry.toRoute<Game>()
                        
                        // Initialize game with current route arguments
                        androidx.compose.runtime.LaunchedEffect(args) {
                            gameViewModel.initGame(args)
                        }

                        GameScreen(
                            viewModel = gameViewModel,
                            onExit = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}