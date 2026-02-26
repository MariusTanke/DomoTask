package com.mariustanke.domotask.core

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.mariustanke.domotask.presentation.login.LoginScreen
import com.mariustanke.domotask.presentation.main.MainScreen
import com.mariustanke.domotask.presentation.register.RegisterScreen
import com.mariustanke.domotask.presentation.splash.SplashScreen

@Composable
fun NavigationWrapper() {
    val backStack = rememberSaveable(saver = backStackSaver()) { mutableStateListOf<Any>(Splash) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 }) togetherWith
                (fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { -it / 4 })
        },
        popTransitionSpec = {
            (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 }) togetherWith
                (fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it / 4 })
        },
        entryProvider = entryProvider {
            entry<Splash> {
                SplashScreen(
                    onLogin = {
                        backStack.clear()
                        backStack.add(Login)
                    },
                    onHome = {
                        backStack.clear()
                        backStack.add(Main)
                    }
                )
            }

            entry<Login> {
                LoginScreen(
                    onRegisterClick = {
                        backStack.add(Register)
                    },
                    onLoginSuccess = {
                        backStack.clear()
                        backStack.add(Main)
                    }
                )
            }

            entry<Register> {
                RegisterScreen(
                    onRegisterSuccess = {
                        backStack.clear()
                        backStack.add(Main)
                    },
                    onBackToLogin = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<Main> {
                MainScreen(
                    onLogoutClick = {
                        backStack.clear()
                        backStack.add(Login)
                    }
                )
            }
        }
    )
}
