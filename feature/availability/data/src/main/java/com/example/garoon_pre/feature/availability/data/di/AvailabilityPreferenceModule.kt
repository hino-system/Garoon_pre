package com.example.garoon_pre.feature.availability.data.di

import com.example.garoon_pre.feature.availability.data.preference.AvailabilityPreferenceRepository
import com.example.garoon_pre.feature.availability.data.preference.DefaultAvailabilityPreferenceRepository
import com.example.garoon_pre.feature.availability.preference.store.LocalPreferenceStoreAdapter
import com.example.garoon_pre.feature.availability.preference.store.PreferenceStoreAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AvailabilityPreferenceStoreModule {

    @Provides
    @Singleton
    fun providePreferenceStoreAdapter(
        local: LocalPreferenceStoreAdapter
    ): PreferenceStoreAdapter {
        return local
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AvailabilityPreferenceRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAvailabilityPreferenceRepository(
        impl: DefaultAvailabilityPreferenceRepository
    ): AvailabilityPreferenceRepository
}