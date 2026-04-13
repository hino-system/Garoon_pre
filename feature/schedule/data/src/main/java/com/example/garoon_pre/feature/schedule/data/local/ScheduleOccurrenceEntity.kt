package com.example.garoon_pre.feature.schedule.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_occurrences",
    indices = [
        Index("dateKey"),
        Index("ownerUserId"),
        Index(value = ["dateKey", "ownerUserId"]),
        Index("scheduleId")
    ]
)
data class ScheduleOccurrenceEntity(
    @PrimaryKey
    val occurrenceId: String,
    val scheduleId: String,
    val dateKey: String,
    val title: String,
    val startAt: String,
    val endAt: String,
    val repeatRule: String,
    val location: String?,
    val description: String?,
    val ownerUserId: String?,
    val organizerName: String,
    val syncedAtEpochMillis: Long
)