package com.example.garoon_pre.feature.board.data.di

import com.example.garoon_pre.feature.board.domain.repository.BoardRepository
import com.example.garoon_pre.feature.board.data.repository.BoardRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BoardRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBoardRepository(
        impl: BoardRepositoryImpl
    ): BoardRepository
}