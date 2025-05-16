package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.model.Board
import com.mariustanke.domotask.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBoardsUseCase @Inject constructor(
    private val repository: BoardRepository
) {
    operator fun invoke(): Flow<List<Board>> = repository.getBoards()
}
