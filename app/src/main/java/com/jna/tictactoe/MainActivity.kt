package com.jna.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jna.tictactoe.game.model.GameMode
import com.jna.tictactoe.navigation.About
import com.jna.tictactoe.navigation.Game
import com.jna.tictactoe.navigation.Lobby
import com.jna.tictactoe.navigation.Menu
import com.jna.tictactoe.navigation.Profile
import com.jna.tictactoe.navigation.Splash
import com.jna.tictactoe.screen.about.AboutScreen
import com.jna.tictactoe.screen.game.GameScreen
import com.jna.tictactoe.screen.game.GameViewModel
import com.jna.tictactoe.screen.lobby.LanLobbyScreen
import com.jna.tictactoe.screen.lobby.LanLobbyViewModel
import com.jna.tictactoe.screen.menu.MainMenuScreen
import com.jna.tictactoe.screen.profile.ProfileScreen
import com.jna.tictactoe.screen.profile.ProfileViewModel
import com.jna.tictactoe.screen.splash.SplashScreen
import com.jna.tictactoe.ui.component.BannerAd
import com.jna.tictactoe.ui.theme.TictactoeTheme
import com.jna.tictactoe.util.AdManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * The main activity of the application, responsible for initializing the navigation graph
 * and setting up the Jetpack Compose UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Entry point for the activity, setting up the UI and navigation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up the edge-to-edge display to use the full screen area
        enableEdgeToEdge()

        // Preload Interstitial Ad
        AdManager.loadInterstitialAd(this)

        setContent {
                TictactoeTheme {
                    val navController = rememberNavController()

                    // Shared ViewModel scoped to the Activity for session-long score persistence
                    val gameViewModel: GameViewModel = hiltViewModel()

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
                            val profileViewModel: ProfileViewModel = hiltViewModel()
                            val userPreferences =
                                profileViewModel.userPreferences.collectAsState().value

                            MainMenuScreen(
                                userPreferences = userPreferences,
                                onVsCpu = { difficulty ->
                                    navController.navigate(
                                        Game(
                                            mode = GameMode.VS_CPU,
                                            difficulty = difficulty
                                        )
                                    )
                                },
                                onVsLocal = {
                                    navController.navigate(Game(mode = GameMode.VS_HUMAN_LOCAL))
                                },
                                onVsLan = {
                                    navController.navigate(Lobby)
                                },
                                onAbout = {
                                    navController.navigate(About)
                                },
                                onProfile = {
                                    navController.navigate(Profile)
                                }
                            )
                        }
                        // About screen with app info and credits
                        composable<About> {
                            AboutScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        // Profile screen for player name and stats
                        composable<Profile> {
                            ProfileScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        // LAN Lobby for discovery and peer connection
                        composable<Lobby> {
                            val lobbyViewModel: LanLobbyViewModel = hiltViewModel()
                            LanLobbyScreen(
                                viewModel = lobbyViewModel,
                                onBack = { navController.popBackStack() },
                                onGameStarted = { peerName, isHost ->
                                    navController.navigate(
                                        Game(
                                            mode = GameMode.VS_LAN,
                                            isHost = isHost,
                                            peerName = peerName
                                        )
                                    ) {
                                        // Remove Lobby from the backstack when game starts
                                        popUpTo(Lobby) { inclusive = true }
                                    }
                                }
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

                        // About screen with app info and credits
                        composable<About> {
                            AboutScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }



            }
        }
    }
}