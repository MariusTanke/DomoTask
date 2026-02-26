package com.mariustanke.domotask.core

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Saver for navigation back stacks that survives configuration changes.
 * Serializes route objects to strings and deserializes them back.
 */
fun backStackSaver(): Saver<SnapshotStateList<Any>, *> = listSaver(
    save = { list ->
        list.map { route -> encodeRoute(route) }
    },
    restore = { saved ->
        mutableStateListOf<Any>().apply {
            addAll(saved.mapNotNull { decodeRoute(it) })
        }
    }
)

private fun encodeRoute(route: Any): String = when (route) {
    is Splash -> "splash"
    is Login -> "login"
    is Register -> "register"
    is Main -> "main"
    is Home -> "home"
    is Profile -> "profile"
    is Board -> "board:${route.boardId}"
    is Ticket -> "ticket:${route.boardId}:${route.ticketId}"
    else -> ""
}

private fun decodeRoute(encoded: String): Any? {
    val parts = encoded.split(":")
    return when (parts[0]) {
        "splash" -> Splash
        "login" -> Login
        "register" -> Register
        "main" -> Main
        "home" -> Home
        "profile" -> Profile
        "board" -> Board(parts.getOrElse(1) { "" })
        "ticket" -> Ticket(parts.getOrElse(1) { "" }, parts.getOrElse(2) { "" })
        else -> null
    }
}
