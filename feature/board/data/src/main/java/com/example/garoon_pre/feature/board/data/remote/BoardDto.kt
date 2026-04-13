package com.example.garoon_pre.feature.board.data.remote

import com.example.garoon_pre.feature.board.domain.model.BoardCategory
import com.example.garoon_pre.feature.board.domain.model.BoardComment
import com.example.garoon_pre.feature.board.domain.model.BoardPost
import com.example.garoon_pre.feature.board.domain.model.BoardPostListResult
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BoardCategoryListResponse(
    val items: List<BoardCategoryDto>
)

@JsonClass(generateAdapter = true)
data class BoardCategoryDto(
    val id: String,
    val name: String,
    val description: String = "",
    val totalPostCount: Int = 0,
    val activePostCount: Int = 0,
    val canPost: Boolean = false,
    val canAdmin: Boolean = false
)

@JsonClass(generateAdapter = true)
data class BoardPostListResponse(
    val category: BoardCategoryHeaderDto,
    val items: List<BoardPostDto>
)

@JsonClass(generateAdapter = true)
data class BoardCategoryHeaderDto(
    val id: String,
    val name: String,
    val description: String = "",
    val canPost: Boolean = false,
    val canAdmin: Boolean = false
)

@JsonClass(generateAdapter = true)
data class BoardPostDto(
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
    val comments: List<BoardCommentDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class BoardCommentDto(
    val id: String,
    val postId: String,
    val body: String,
    val authorUserId: String,
    val authorName: String,
    val createdAt: String,
    val updatedAt: String,
    val canDelete: Boolean = false
)

fun BoardCategoryDto.toDomain(): BoardCategory {
    return BoardCategory(
        id = id,
        name = name,
        description = description,
        totalPostCount = totalPostCount,
        activePostCount = activePostCount,
        canPost = canPost,
        canAdmin = canAdmin
    )
}

fun BoardCategoryHeaderDto.toDomain(): BoardCategory {
    return BoardCategory(
        id = id,
        name = name,
        description = description,
        totalPostCount = 0,
        activePostCount = 0,
        canPost = canPost,
        canAdmin = canAdmin
    )
}

fun BoardCommentDto.toDomain(): BoardComment {
    return BoardComment(
        id = id,
        postId = postId,
        body = body,
        authorUserId = authorUserId,
        authorName = authorName,
        createdAt = createdAt,
        updatedAt = updatedAt,
        canDelete = canDelete
    )
}

fun BoardPostDto.toDomain(): BoardPost {
    return BoardPost(
        id = id,
        categoryId = categoryId,
        categoryName = categoryName,
        targetDepartment1 = targetDepartment1,
        title = title,
        body = body,
        startAt = startAt,
        endAt = endAt,
        allowComments = allowComments,
        authorUserId = authorUserId,
        authorName = authorName,
        createdAt = createdAt,
        updatedAt = updatedAt,
        status = status,
        canEdit = canEdit,
        canDelete = canDelete,
        canComment = canComment,
        commentCount = commentCount,
        comments = comments.map { it.toDomain() }
    )
}

fun BoardPostListResponse.toDomain(): BoardPostListResult {
    return BoardPostListResult(
        category = category.toDomain(),
        items = items.map { it.toDomain() }
    )
}