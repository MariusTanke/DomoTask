package com.mariustanke.domotask.domain.model

data class Board(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val members: List<String> = emptyList()
)
