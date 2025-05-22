package com.mariustanke.domotask.domain.model

data class Ticket(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val urgency: String = "",
    val status: String = "",
    val createdBy: String = "",
    val assignedTo: String = "",
    val edited: Boolean = false,
    val parent: String? = "",
    val createdAt: Long = System.currentTimeMillis()
)
