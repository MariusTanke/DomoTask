package com.mariustanke.domotask.presentation.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.mariustanke.domotask.R
import com.mariustanke.domotask.core.*
import com.mariustanke.domotask.presentation.board.BoardScreen
import com.mariustanke.domotask.presentation.home.HomeScreen
import com.mariustanke.domotask.presentation.profile.ProfileScreen
import com.mariustanke.domotask.presentation.ticket.TicketScreen

private const val LOGOUT_KEY = "logout"

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val backStack = rememberSaveable(saver = backStackSaver()) { mutableStateListOf<Any>(Home) }

    val currentScreen = backStack.lastOrNull()

    val sideItems = listOf(
        BottomNavItem(Profile, stringResource(R.string.nav_profile), Icons.Default.Person),
        BottomNavItem(LOGOUT_KEY, stringResource(R.string.nav_logout), Icons.AutoMirrored.Filled.ExitToApp)
    )

    Scaffold(
        bottomBar = {
            Box {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(88.dp)
                ) {
                    // Profile (left)
                    val profileItem = sideItems[0]
                    val profileSelected = currentScreen is Profile
                    NavigationBarItem(
                        selected = profileSelected,
                        onClick = {
                            backStack.removeRange(1, backStack.size)
                            backStack[0] = profileItem.key
                        },
                        icon = {
                            Icon(
                                profileItem.icon,
                                contentDescription = profileItem.label,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        label = {
                            Text(
                                profileItem.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                            selectedTextColor = MaterialTheme.colorScheme.onSecondary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f)
                        )
                    )

                    // Spacer for center button
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = {},
                        label = {},
                        enabled = false,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.secondary
                        )
                    )

                    // Logout (right)
                    val logoutItem = sideItems[1]
                    NavigationBarItem(
                        selected = false,
                        onClick = { showLogoutDialog = true },
                        icon = {
                            Icon(
                                logoutItem.icon,
                                contentDescription = logoutItem.label,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        label = {
                            Text(
                                logoutItem.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                            selectedTextColor = MaterialTheme.colorScheme.onSecondary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f)
                        )
                    )
                }

                // Floating center Home button with logo — light blue bg, blue border
                FloatingActionButton(
                    onClick = {
                        backStack.removeRange(1, backStack.size)
                        backStack[0] = Home
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-2).dp)
                        .size(77.dp)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    Image(
                        painter = painterResource(R.drawable.dometask_logo_small),
                        contentDescription = stringResource(R.string.home_title),
                        modifier = Modifier.size(65.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavDisplay(
                backStack = backStack,
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                },
                transitionSpec = {
                    (fadeIn(tween(250)) + slideInHorizontally(tween(300)) { it / 3 }) togetherWith
                        (fadeOut(tween(250)) + slideOutHorizontally(tween(300)) { -it / 3 })
                },
                popTransitionSpec = {
                    (fadeIn(tween(250)) + slideInHorizontally(tween(300)) { -it / 3 }) togetherWith
                        (fadeOut(tween(250)) + slideOutHorizontally(tween(300)) { it / 3 })
                },
                entryProvider = entryProvider {
                    entry<Home> {
                        HomeScreen(
                            onNavigateToBoard = { boardId ->
                                backStack.add(Board(boardId))
                            }
                        )
                    }

                    entry<Board> { key ->
                        BoardScreen(
                            boardId = key.boardId,
                            onTicketClick = { bId, tId ->
                                backStack.add(Ticket(bId, tId))
                            },
                            onBackClick = {
                                backStack.removeLastOrNull()
                            }
                        )
                    }

                    entry<Ticket> { key ->
                        TicketScreen(
                            boardId = key.boardId,
                            ticketId = key.ticketId,
                            onBackToBoardClick = {
                                backStack.removeLastOrNull()
                            },
                            onSubTicketClick = { bId, tId ->
                                backStack.add(Ticket(bId, tId))
                            }
                        )
                    }

                    entry<Profile> {
                        ProfileScreen()
                    }
                }
            )
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

data class BottomNavItem(val key: Any, val label: String, val icon: ImageVector)
