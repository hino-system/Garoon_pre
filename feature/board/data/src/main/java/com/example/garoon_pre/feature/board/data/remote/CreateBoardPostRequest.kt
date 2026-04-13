package com.example.garoon_pre.feature.board.data.remote

import com.squareup.moshi.JsonClass

data class CreateBoardPostRequest(
    val categoryId: String,
    val title: String,
    val body: String,
    val startAt: String,
    val endAt: String,
    val allowComments: Boolean = true,
    val targetDepartment1: String? = null
)

data class UpdateBoardPostRequest(
    val title: String,
    val body: String,
    val startAt: String,
    val endAt: String,
    val allowComments: Boolean = true,
    val targetDepartment1: String? = null
)

data class CreateBoardCommentRequest(
    val body: String
)

@JsonClass(generateAdapter = true)
data class OkResponse(
    val ok: Boolean
)