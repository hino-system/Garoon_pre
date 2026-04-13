package com.example.garoon_pre.feature.user.data.remote

import com.example.garoon_pre.core.model.user.GaroonUser
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserListResponse(
    val items: List<UserDto>
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val userId: String,
    val displayName: String,
    val department1: String? = null,
    val department2: String? = null,
    val position: String = "",
    val role: String = ""
)

fun UserDto.toDomain(): GaroonUser {
    return GaroonUser(
        id = id,
        userId = userId,
        displayName = displayName,
        department1 = department1,
        department2 = department2,
        position = position,
        role = role
    )
}