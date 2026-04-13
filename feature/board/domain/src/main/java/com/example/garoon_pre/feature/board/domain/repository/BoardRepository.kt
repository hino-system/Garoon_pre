package com.example.garoon_pre.feature.board.domain.repository

import com.example.garoon_pre.feature.board.domain.model.BoardCategory
import com.example.garoon_pre.feature.board.domain.model.BoardComment
import com.example.garoon_pre.feature.board.domain.model.BoardPost
import com.example.garoon_pre.feature.board.domain.model.BoardPostListResult

interface BoardRepository {
    suspend fun getBoardCategories(): List<BoardCategory>
    suspend fun getBoardPosts(categoryId: String): BoardPostListResult
    suspend fun getBoardPostDetail(postId: String): BoardPost

    suspend fun createBoardPost(
        categoryId: String,
        title: String,
        body: String,
        startAt: String,
        endAt: String,
        allowComments: Boolean,
        targetDepartment1: String?
    ): BoardPost

    suspend fun updateBoardPost(
        postId: String,
        title: String,
        body: String,
        startAt: String,
        endAt: String,
        allowComments: Boolean,
        targetDepartment1: String?
    ): BoardPost

    suspend fun deleteBoardPost(postId: String)
    suspend fun createBoardComment(postId: String, body: String): BoardComment
    suspend fun deleteBoardComment(commentId: String)
}