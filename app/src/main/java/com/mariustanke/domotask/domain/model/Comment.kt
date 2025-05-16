package com.mariustanke.domotask.domain.model

data class Comment(
    val id: String = "",
    val createdBy: String = "",
    val content: String = "",
    val edited: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
