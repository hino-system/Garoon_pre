package com.example.garoon_pre.feature.schedule.data.di

import com.example.garoon_pre.feature.schedule.data.remote.ScheduleApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ScheduleApiModule {

    @Provides
    fun provideScheduleApi(retrofit: Retrofit): ScheduleApi {
        return retrofit.create(ScheduleApi::class.java)
    }
}