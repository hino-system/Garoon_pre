package com.example.garoon_pre.feature.schedule.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleOccurrenceDao {

    @Query(
        """
        SELECT * FROM schedule_occurrences
        WHERE dateKey = :dateKey
        ORDER BY startAt ASC, organizerName ASC, title ASC
        """
    )
    fun observeByDate(dateKey: String): Flow<List<ScheduleOccurrenceEntity>>

    @Query(
        """
        SELECT * FROM schedule_occurrences
        WHERE dateKey = :dateKey
          AND ownerUserId IN (:ownerUserIds)
        ORDER BY startAt ASC, organizerName ASC, title ASC
        """
    )
    fun observeByDateForUsers(
        dateKey: String,
        ownerUserIds: List<String>
    ): Flow<List<ScheduleOccurrenceEntity>>

    @Query(
        """
        SELECT * FROM schedule_occurrences
        WHERE dateKey BETWEEN :startDateKey AND :endDateKey
        ORDER BY dateKey ASC, startAt ASC, organizerName ASC, title ASC
        """
    )
    fun observeRange(
        startDateKey: String,
        endDateKey: String
    ): Flow<List<ScheduleOccurrenceEntity>>

    @Query(
        """
        SELECT * FROM schedule_occurrences
        WHERE dateKey BETWEEN :startDateKey AND :endDateKey
          AND ownerUserId IN (:ownerUserIds)
        ORDER BY dateKey ASC, startAt ASC, organizerName ASC, title ASC
        """
    )
    fun observeRangeForUsers(
        startDateKey: String,
        endDateKey: String,
        ownerUserIds: List<String>
    ): Flow<List<ScheduleOccurrenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ScheduleOccurrenceEntity>)

    @Query(
        """
        DELETE FROM schedule_occurrences
        WHERE dateKey = :dateKey
        """
    )
    suspend fun deleteByDate(dateKey: String)

    @Query(
        """
        DELETE FROM schedule_occurrences
        WHERE dateKey = :dateKey
          AND ownerUserId IN (:ownerUserIds)
        """
    )
    suspend fun deleteByDateAndOwnerUsers(
        dateKey: String,
        ownerUserIds: List<String>
    )
}