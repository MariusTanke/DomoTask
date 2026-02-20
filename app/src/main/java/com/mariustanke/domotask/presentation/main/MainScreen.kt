package com.mariustanke.domotask.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.mariustanke.domotask.R
import com.mariustanke.domotask.core.Screen
import com.mariustanke.domotask.presentation.board.BoardScreen
import com.mariustanke.domotask.presentation.home.HomeScreen
import com.mariustanke.domotask.presentation.profile.ProfileScreen
import com.mariustanke.domotask.presentation.ticket.TicketScreen

private const val LOGOUT_ROUTE = "logout"

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    val items = listOf(
        BottomNavItem(Screen.Home.route, stringResource(R.string.home_title), Icons.Default.Home),
        BottomNavItem(Screen.Profile.route, stringResource(R.string.nav_profile), Icons.Default.Person),
        BottomNavItem(LOGOUT_ROUTE, stringResource(R.string.nav_logout), Icons.AutoMirrored.Filled.ExitToApp)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination == item.route && item.route != LOGOUT_ROUTE,
                        onClick = {
                            if (item.route == LOGOUT_ROUTE) {
                                showLogoutDialog = true
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
                    onNavigateToBoard = { boardId ->
                        navController.navigate(Screen.Board.createRoute(boardId))
                    }
                )
            }
            composable(
                route = Screen.Board.route,
                arguments = listOf(navArgument("boardId") { type = NavType.StringType })
            ) { backStackEntry ->
                val boardId = backStackEntry.arguments?.getString("boardId") ?: ""
                BoardScreen(
                    boardId = boardId,
                    onTicketClick = { bId, tId ->
                        navController.navigate(Screen.Ticket.createRoute(bId, tId))
                    },
                    onBackClick = { navController.navigate(Screen.Home.route) }
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
                TicketScreen(
                    boardId = boardId,
                    ticketId = ticketId,
                    onBackToBoardClick = {
                        navController.navigate(Screen.Board.createRoute(boardId))
                    },
                    onSubTicketClick = { bId, tId ->
                        navController.navigate(Screen.Ticket.createRoute(bId, tId))
                    }
                )
            }
            composable(Screen.Profile.route) { ProfileScreen() }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text(stringResource(R.string.logout_confirm_title)) },
                text = { Text("") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.signOut()
                        onLogoutClick()
                        showLogoutDialog = false
                    }) {
                        Text(stringResource(R.string.logout_confirm_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)
