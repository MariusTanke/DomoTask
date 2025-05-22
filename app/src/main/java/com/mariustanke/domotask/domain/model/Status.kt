// Status.kt
package com.mariustanke.domotask.domain.model

import java.util.UUID

data class Status(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val order: Int = 0
)