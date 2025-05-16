package com.mariustanke.domotask.domain.usecase.auth

import javax.inject.Inject

class AuthUseCases @Inject constructor(
    val getUser: GetUserUseCase
)