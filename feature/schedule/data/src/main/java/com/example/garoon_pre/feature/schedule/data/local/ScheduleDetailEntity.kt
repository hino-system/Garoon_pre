package com.example.garoon_pre.feature.schedule.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_details")
data class ScheduleDetailEntity(
    @PrimaryKey
    val scheduleId: String,
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