package com.Sufi.zoodex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.Sufi.zoodex.ui.screens.*
import com.Sufi.zoodex.ui.theme.MidnightSpace
import com.Sufi.zoodex.ui.theme.ZoodexTheme

import com.Sufi.zoodex.data.GameState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check SharedPreferences for existing profile
        val prefs = getSharedPreferences("zoodex", MODE_PRIVATE)
        val savedCallsign = prefs.getString("callsign", "") ?: ""
        val savedFaction = prefs.getString("faction", "") ?: ""
        val hasProfile = savedCallsign.isNotBlank()

        setContent {
            var darkThemeEnabled by remember { mutableStateOf(GameState.isDarkTheme) }

            // Dynamic recomposition trigger for theme selection
            LaunchedEffect(GameState.isDarkTheme) {
                darkThemeEnabled = GameState.isDarkTheme
            }

            ZoodexTheme(darkTheme = darkThemeEnabled) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val startDest = "splash"

                    NavHost(navController = navController, startDestination = startDest) {

                        composable("splash") {
                            SplashScreen(onSplashComplete = {
                                if (hasProfile) {
                                    navController.navigate("home/$savedCallsign/$savedFaction") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("setup") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            })
                        }

                        composable("setup") {
                            FirstTimeSetupScreen(onSetupComplete = { callsign, faction ->
                                prefs.edit()
                                    .putString("callsign", callsign)
                                    .putString("faction", faction)
                                    .apply()
                                navController.navigate("home/$callsign/$faction") {
                                    popUpTo("setup") { inclusive = true }
                                }
                            })
                        }

                        composable(
                            "home/{callsign}/{faction}",
                            arguments = listOf(
                                navArgument("callsign") { type = NavType.StringType },
                                navArgument("faction") { type = NavType.StringType }
                            )
                        ) { backStack ->
                            val callsign = backStack.arguments?.getString("callsign") ?: ""
                            val faction = backStack.arguments?.getString("faction") ?: ""
                            CommandHubScreen(callsign = callsign, faction = faction) { route ->
                                navController.navigate(route)
                            }
                        }

                        composable("map") {
                            MapScreen(onBack = { navController.popBackStack() })
                        }

                        composable("scanner") {
                            ScannerScreen(
                                onBack = { navController.popBackStack() },
                                onCapture = { _ ->
                                    // Navigate to encyclopedia after capture
                                    navController.navigate("encyclopedia") {
                                        popUpTo("scanner") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("encyclopedia") {
                            EncyclopediaScreen(
                                onBack = { navController.popBackStack() },
                                onBeastDetail = { id -> navController.navigate("beast/$id") }
                            )
                        }

                        composable(
                            "beast/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.IntType })
                        ) { backStack ->
                            val id = backStack.arguments?.getInt("id") ?: 1
                            BeastDetailScreen(beastId = id, onBack = { navController.popBackStack() })
                        }

                        composable("arena") {
                            ArenaScreen(onBack = { navController.popBackStack() })
                        }

                        composable("shop") {
                            ShopScreen(onBack = { navController.popBackStack() })
                        }

                        composable("comms") {
                            CommsScreen(
                                onBack = { navController.popBackStack() },
                                onLaunchBattle = { navController.navigate("arena") }
                            )
                        }

                        composable("teams") {
                            TeamsScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
