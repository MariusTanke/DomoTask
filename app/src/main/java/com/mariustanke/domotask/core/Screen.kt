package com.mariustanke.domotask.core

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Main : Screen("main")
    object Register : Screen("register")
}