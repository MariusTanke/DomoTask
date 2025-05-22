package com.mariustanke.domotask.domain.usecase.board

import com.mariustanke.domotask.domain.model.Board
import com.mariustanke.domotask.domain.repository.BoardRepository
import javax.inject.Inject

class UpdateBoardUseCase @Inject constructor(
    private val repository: BoardRepository
) {
    suspend operator fun invoke(board: Board) {
        repository.updateBoard(board)
    }
}