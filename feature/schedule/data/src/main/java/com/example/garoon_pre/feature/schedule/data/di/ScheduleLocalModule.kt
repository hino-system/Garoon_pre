package com.example.garoon_pre.feature.schedule.data.di

import android.content.Context
import androidx.room.Room
import com.example.garoon_pre.feature.schedule.data.local.ScheduleDatabase
import com.example.garoon_pre.feature.schedule.data.local.ScheduleDetailDao
import com.example.garoon_pre.feature.schedule.data.local.ScheduleOccurrenceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScheduleLocalModule {

    @Provides
    @Singleton
    fun provideScheduleDatabase(
        @ApplicationContext context: Context
    ): ScheduleDatabase {
        return Room.databaseBuilder(
            context,
            ScheduleDatabase::class.java,
            "schedule.db"
        ).build()
    }

    @Provides
    fun provideScheduleOccurrenceDao(
        db: ScheduleDatabase
    ): ScheduleOccurrenceDao {
        return db.scheduleOccurrenceDao()
    }

    @Provides
    fun provideScheduleDetailDao(
        db: ScheduleDatabase
    ): ScheduleDetailDao {
        return db.scheduleDetailDao()
    }
}