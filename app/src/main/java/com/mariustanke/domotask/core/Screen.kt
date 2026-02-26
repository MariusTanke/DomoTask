package com.mariustanke.domotask.core

import kotlinx.serialization.Serializable

@Serializable
data object Splash

@Serializable
data object Login

@Serializable
data object Register

@Serializable
data object Main

@Serializable
data object Home

@Serializable
data object Profile

@Serializable
data class Board(val boardId: String)

@Serializable
data class Ticket(val boardId: String, val ticketId: String)
