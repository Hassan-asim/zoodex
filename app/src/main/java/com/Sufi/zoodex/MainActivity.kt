package com.Sufi.zoodex

import android.net.Uri
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

        // Keep profile persistence aligned with GameState storage.
        val prefs = getSharedPreferences("zoodex_save", MODE_PRIVATE)
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
                            MapScreen(
                                onBack = { navController.popBackStack() },
                                onBattle = { navController.navigate("arena/ai/NONE") }
                            )
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

                        composable(
                            "arena/{mode}/{friend}",
                            arguments = listOf(
                                navArgument("mode") { type = NavType.StringType; defaultValue = "ai" },
                                navArgument("friend") { type = NavType.StringType; defaultValue = "NONE" }
                            )
                        ) { backStack ->
                            val mode = backStack.arguments?.getString("mode") ?: "ai"
                            val friend = backStack.arguments?.getString("friend") ?: "NONE"
                            ArenaScreen(
                                navMode = mode,
                                navFriendEncoded = friend,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("shop") {
                            ShopScreen(onBack = { navController.popBackStack() })
                        }

                        composable("comms") {
                            CommsScreen(
                                onBack = { navController.popBackStack() },
                                onLaunchBattle = { friendTag ->
                                    if (friendTag.isNullOrBlank()) {
                                        navController.navigate("arena/ai/NONE")
                                    } else {
                                        navController.navigate("arena/friend/${Uri.encode(friendTag)}")
                                    }
                                }
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
