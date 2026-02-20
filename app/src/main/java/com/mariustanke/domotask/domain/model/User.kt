package com.mariustanke.domotask.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photo: String? = null,
    val invitationCode: String = "",
    val invitations: List<String> = emptyList(),
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
