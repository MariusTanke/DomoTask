package com.mariustanke.domotask.core

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Settings : Screen("settings")

    object Board : Screen("board/{boardId}") {
        fun createRoute(boardId: String) = "board/$boardId"
    }

    object Ticket : Screen("ticket/{boardId}/{ticketId}") {
        fun createRoute(boardId: String, ticketId: String) = "ticket/$boardId/$ticketId"
    }
}
