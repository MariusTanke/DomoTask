package com.mariustanke.domotask.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.mariustanke.domotask.presentation.home.HomeScreen
import com.mariustanke.domotask.presentation.profile.ProfileScreen
import com.mariustanke.domotask.presentation.settings.SettingsScreen

@Composable
fun MainScreen(
    onLogoutClick: () -> Unit
) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    val items = listOf(
        BottomNavItem("home", "Inicio", Icons.Default.Home),
        BottomNavItem("profile", "Perfil", Icons.Default.Person),
        BottomNavItem("settings", "Ajustes", Icons.Default.Settings),
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
                                FirebaseAuth.getInstance().signOut()
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
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("profile") { ProfileScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
