// Board.kt
package com.mariustanke.domotask.domain.model

data class Board(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val statuses: List<Status> = emptyList(),
    val createdAt: Long = 0L,
    val members: List<String> = emptyList()
)
