package com.example.garoon_pre.feature.auth.domain.repository

interface AuthRepository {
    suspend fun login(userId: String, password: String)
}