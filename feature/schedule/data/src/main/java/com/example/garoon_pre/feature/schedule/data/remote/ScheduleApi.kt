package com.example.garoon_pre.feature.schedule.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleApi {

    @GET("api/v1/schedules")
    suspend fun getSchedules(
        @Header("Authorization") authorization: String,
        @Query("date") date: String,
        @Query("userIds") userIds: String? = null
    ): ScheduleListResponse

    @GET("api/v1/schedules/{id}")
    suspend fun getScheduleById(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): ScheduleDto

    @POST("api/v1/schedules")
    suspend fun createSchedule(
        @Header("Authorization") authorization: String,
        @Body request: CreateScheduleRequest
    ): ScheduleDto

    @PUT("api/v1/schedules/{id}")
    suspend fun updateSchedule(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Body request: CreateScheduleRequest
    ): ScheduleDto
}