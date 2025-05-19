package com.mariustanke.domotask.domain.usecase.auth

import javax.inject.Inject

class UserUseCases @Inject constructor(
    val getUser: GetUserUseCase,
    val createUser: CreateUserUseCase,
    val updateFcmToken: UpdateFcmTokenUseCase
)