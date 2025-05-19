package com.mariustanke.domotask.presentation.main

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.mariustanke.domotask.core.Screen
import com.mariustanke.domotask.presentation.board.BoardScreen
import com.mariustanke.domotask.presentation.home.HomeScreen
import com.mariustanke.domotask.presentation.profile.ProfileScreen
import com.mariustanke.domotask.presentation.settings.SettingsScreen
import com.mariustanke.domotask.presentation.ticket.TicketScreen

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit
) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    val items = listOf(
        BottomNavItem(Screen.Home.route, "Inicio", Icons.Default.Home),
        BottomNavItem(Screen.Profile.route, "Perfil", Icons.Default.Person),
        BottomNavItem(Screen.Settings.route, "Ajustes", Icons.Default.Settings),
        BottomNavItem("logout", "Salir", Icons.Default.ExitToApp)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination == item.route,
                        onClick = {
                            if (item.route == "logout") {
                                viewModel.signOut()
                                onLogoutClick()

                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToBoard = { boardId, boardName ->
                        navController.navigate(Screen.Board.createRoute(boardId, Uri.encode(boardName)))
                    }
                )
            }
            composable(
                route = Screen.Board.route,
                arguments = listOf(
                    navArgument("boardId") { type = NavType.StringType },
                    navArgument("boardName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val boardId = backStackEntry.arguments?.getString("boardId") ?: ""
                val boardName = backStackEntry.arguments?.getString("boardName") ?: ""
                BoardScreen(
                    boardId = boardId,
                    boardName = boardName,
                    onTicketClick = { bId, tId ->
                        navController.navigate(Screen.Ticket.createRoute(bId, tId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Ticket.route,
                arguments = listOf(
                    navArgument("boardId") { type = NavType.StringType },
                    navArgument("ticketId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val boardId = backStackEntry.arguments?.getString("boardId") ?: return@composable
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: return@composable
                TicketScreen(boardId = boardId, ticketId = ticketId, onBackClick = {
                    navController.popBackStack()
                })
            }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
