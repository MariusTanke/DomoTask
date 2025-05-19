package com.mariustanke.domotask.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val invitationCode: String = "",
    val invitations: ArrayList<String> = ArrayList(),
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
