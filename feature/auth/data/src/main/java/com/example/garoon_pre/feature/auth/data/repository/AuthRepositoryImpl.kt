package com.example.garoon_pre.feature.auth.data.repository

import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.auth.data.remote.AuthApi
import com.example.garoon_pre.feature.auth.data.remote.LoginRequest
import com.example.garoon_pre.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionStore: SessionStore
) : AuthRepository {

    override suspend fun login(userId: String, password: String) {
        val response = api.login(
            LoginRequest(
                userId = userId,
                password = password
            )
        )

        sessionStore.saveSession(
            authUserId = response.user.id,
            userId = response.user.userId,
            token = response.token,
            displayName = response.user.displayName.ifBlank { response.user.userId },
            department1 = response.user.department1,
            department2 = response.user.department2,
            position = response.user.position,
            role = response.user.role
        )
    }
}