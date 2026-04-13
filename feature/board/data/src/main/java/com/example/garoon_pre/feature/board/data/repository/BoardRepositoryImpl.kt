package com.example.garoon_pre.feature.board.data.repository

import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.board.data.remote.BoardApi
import com.example.garoon_pre.feature.board.data.remote.CreateBoardCommentRequest
import com.example.garoon_pre.feature.board.data.remote.CreateBoardPostRequest
import com.example.garoon_pre.feature.board.data.remote.UpdateBoardPostRequest
import com.example.garoon_pre.feature.board.data.remote.toDomain
import com.example.garoon_pre.feature.board.domain.model.BoardCategory
import com.example.garoon_pre.feature.board.domain.model.BoardComment
import com.example.garoon_pre.feature.board.domain.model.BoardPost
import com.example.garoon_pre.feature.board.domain.model.BoardPostListResult
import com.example.garoon_pre.feature.board.domain.repository.BoardRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class BoardRepositoryImpl @Inject constructor(
    private val api: BoardApi,
    private val sessionStore: SessionStore
) : BoardRepository {

    private suspend fun authHeader(): String {
        val token = sessionStore.tokenFlow.first()
        check(token.isNotBlank()) { "ログインしてください" }
        return "Bearer $token"
    }

    override suspend fun getBoardCategories(): List<BoardCategory> {
        val token = sessionStore.tokenFlow.first()
        if (token.isBlank()) return emptyList()

        return api.getBoardCategories(
            authorization = "Bearer $token"
        ).items.map { it.toDomain() }
    }

    override suspend fun getBoardPosts(categoryId: String): BoardPostListResult {
        return api.getBoardPosts(
            authorization = authHeader(),
            categoryId = categoryId
        ).toDomain()
    }

    override suspend fun getBoardPostDetail(postId: String): BoardPost {
        return api.getBoardPostDetail(
            authorization = authHeader(),
            id = postId
        ).toDomain()
    }

    override suspend fun createBoardPost(
        categoryId: String,
        title: String,
        body: String,
        startAt: String,
        endAt: String,
        allowComments: Boolean,
        targetDepartment1: String?
    ): BoardPost {
        return api.createBoardPost(
            authorization = authHeader(),
            request = CreateBoardPostRequest(
                categoryId = categoryId,
                title = title,
                body = body,
                startAt = startAt,
                endAt = endAt,
                allowComments = allowComments,
                targetDepartment1 = targetDepartment1
            )
        ).toDomain()
    }

    override suspend fun updateBoardPost(
        postId: String,
        title: String,
        body: String,
        startAt: String,
        endAt: String,
        allowComments: Boolean,
        targetDepartment1: String?
    ): BoardPost {
        return api.updateBoardPost(
            authorization = authHeader(),
            id = postId,
            request = UpdateBoardPostRequest(
                title = title,
                body = body,
                startAt = startAt,
                endAt = endAt,
                allowComments = allowComments,
                targetDepartment1 = targetDepartment1
            )
        ).toDomain()
    }

    override suspend fun deleteBoardPost(postId: String) {
        api.deleteBoardPost(
            authorization = authHeader(),
            id = postId
        )
    }

    override suspend fun createBoardComment(postId: String, body: String): BoardComment {
        return api.createBoardComment(
            authorization = authHeader(),
            id = postId,
            request = CreateBoardCommentRequest(body = body)
        ).toDomain()
    }

    override suspend fun deleteBoardComment(commentId: String) {
        api.deleteBoardComment(
            authorization = authHeader(),
            id = commentId
        )
    }
}