package com.example.garoon_pre.feature.availability.data.preference.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT

interface AvailabilityPreferenceApi {

    @GET("api/v1/me/availability-preference")
    suspend fun getAvailabilityPreference(
        @Header("Authorization") authorization: String
    ): AvailabilityPreferenceDto

    @PUT("api/v1/me/availability-preference")
    suspend fun putAvailabilityPreference(
        @Header("Authorization") authorization: String,
        @Body request: AvailabilityPreferenceDto
    )
}