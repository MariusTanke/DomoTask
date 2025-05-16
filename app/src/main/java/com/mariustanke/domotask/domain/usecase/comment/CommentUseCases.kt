package com.mariustanke.domotask.domain.usecase.comment

data class CommentUseCases(
    val getComments: GetCommentsUseCase,
    val addComment: AddCommentUseCase,
    val updateComment: UpdateCommentUseCase,
    val deleteComment: DeleteCommentUseCase
)