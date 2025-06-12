package com.mariustanke.domotask.domain.usecase.auth

import javax.inject.Inject

class UserUseCases @Inject constructor(
    val getUser: GetUserUseCase,
    val getUserFlow: GetUserFlowUseCase,
    val createUser: CreateUserUseCase,
    val updateUser: UpdateUserUseCase,
    val updateFcmToken: UpdateFcmTokenUseCase
)