package com.example.garoon_pre.feature.board.domain.model

data class BoardCategory(
    val id: String,
    val name: String,
    val description: String,
    val totalPostCount: Int = 0,
    val activePostCount: Int = 0,
    val canPost: Boolean = false,
    val canAdmin: Boolean = false
)

data class BoardComment(
    val id: String,
    val postId: String,
    val body: String,
    val authorUserId: String,
    val authorName: String,
    val createdAt: String,
    val updatedAt: String,
    val canDelete: Boolean = false
)

data class BoardPost(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val targetDepartment1: String? = null,
    val title: String,
    val body: String,
    val startAt: String,
    val endAt: String,
    val allowComments: Boolean = true,
    val authorUserId: String,
    val authorName: String,
    val createdAt: String,
    val updatedAt: String,
    val status: String = "active",
    val canEdit: Boolean = false,
    val canDelete: Boolean = false,
    val canComment: Boolean = false,
    val commentCount: Int = 0,
    val comments: List<BoardComment> = emptyList()
)

data class BoardPostListResult(
    val category: BoardCategory,
    val items: List<BoardPost>
)