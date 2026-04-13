package com.example.garoon_pre.feature.board.data.di

import com.example.garoon_pre.feature.board.data.remote.BoardApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object BoardApiModule {

    @Provides
    fun provideBoardApi(retrofit: Retrofit): BoardApi {
        return retrofit.create(BoardApi::class.java)
    }
}