package com.example.garoon_pre.feature.user.data.remote

import retrofit2.http.GET
import retrofit2.http.Header

interface UserApi {

    @GET("api/v1/users")
    suspend fun getUsers(
        @Header("Authorization") authorization: String
    ): UserListResponse
}