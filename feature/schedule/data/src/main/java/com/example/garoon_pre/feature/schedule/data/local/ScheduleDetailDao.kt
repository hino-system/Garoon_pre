package com.example.garoon_pre.feature.schedule.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDetailDao {

    @Query(
        """
        SELECT * FROM schedule_details
        WHERE scheduleId = :scheduleId
        LIMIT 1
        """
    )
    fun observeById(scheduleId: String): Flow<ScheduleDetailEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ScheduleDetailEntity)
}