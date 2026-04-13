package com.example.garoon_pre.feature.availability.data.di

import com.example.garoon_pre.feature.availability.data.preference.remote.AvailabilityPreferenceApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object AvailabilityPreferenceApiModule {

    @Provides
    fun provideAvailabilityPreferenceApi(retrofit: Retrofit): AvailabilityPreferenceApi {
        return retrofit.create(AvailabilityPreferenceApi::class.java)
    }
}