package com.example.garoon_pre.feature.schedule.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ScheduleOccurrenceEntity::class,
        ScheduleDetailEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ScheduleDatabase : RoomDatabase() {
    abstract fun scheduleOccurrenceDao(): ScheduleOccurrenceDao
    abstract fun scheduleDetailDao(): ScheduleDetailDao
}