package dev.mercemay.lumen.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.mercemay.lumen.ui.screens.AboutScreen
import dev.mercemay.lumen.ui.screens.AdvancedSettingsScreen
import dev.mercemay.lumen.ui.screens.HistoryScreen
import dev.mercemay.lumen.ui.screens.HomeScreen
import dev.mercemay.lumen.ui.screens.SettingsScreen
import dev.mercemay.lumen.ui.screens.WorkerSettingsScreen

private enum class Destination(val route: String, val label: String) {
    Home("home", "测速"),
    History("history", "历史"),
    Settings("settings", "设置"),
}

@Composable
fun LumenNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route
    val showBottomBar = Destination.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                            onClick = {
                                if (destination.route != currentRoute) {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                val icon = when (destination) {
                                    Destination.Home -> Icons.Default.Home
                                    Destination.History -> Icons.Default.History
                                    Destination.Settings -> Icons.Default.Settings
                                }
                                Icon(icon, contentDescription = destination.label)
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Destination.Home.route) { HomeScreen() }
            composable(Destination.History.route) { HistoryScreen() }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    onOpenWorker = { navController.navigate("worker-settings") },
                    onOpenAdvancedSettings = { navController.navigate("advanced-settings") },
                    onOpenAbout = { navController.navigate("about") },
                )
            }
            composable("worker-settings") { WorkerSettingsScreen(onBack = { navController.popBackStack() }) }
            composable("advanced-settings") { AdvancedSettingsScreen(onBack = { navController.popBackStack() }) }
            composable("about") { AboutScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
