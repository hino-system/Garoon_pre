package com.example.garoon_pre.feature.auth.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val userId: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val user: AuthUserDto
)

@JsonClass(generateAdapter = true)
data class AuthUserDto(
    val id: String,
    val userId: String,
    val displayName: String,
    val department1: String? = null,
    val department2: String? = null,
    val position: String = "",
    val role: String = ""
)