package com.example.garoon_pre.feature.board.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BoardApi {

    @GET("api/v1/board-categories")
    suspend fun getBoardCategories(
        @Header("Authorization") authorization: String
    ): BoardCategoryListResponse

    @GET("api/v1/board-posts")
    suspend fun getBoardPosts(
        @Header("Authorization") authorization: String,
        @Query("categoryId") categoryId: String
    ): BoardPostListResponse

    @GET("api/v1/board-posts/{id}")
    suspend fun getBoardPostDetail(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): BoardPostDto

    @POST("api/v1/board-posts")
    suspend fun createBoardPost(
        @Header("Authorization") authorization: String,
        @Body request: CreateBoardPostRequest
    ): BoardPostDto

    @PUT("api/v1/board-posts/{id}")
    suspend fun updateBoardPost(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body request: UpdateBoardPostRequest
    ): BoardPostDto

    @DELETE("api/v1/board-posts/{id}")
    suspend fun deleteBoardPost(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): OkResponse

    @POST("api/v1/board-posts/{id}/comments")
    suspend fun createBoardComment(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body request: CreateBoardCommentRequest
    ): BoardCommentDto

    @DELETE("api/v1/board-comments/{id}")
    suspend fun deleteBoardComment(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): OkResponse
}