package com.example.garoon_pre.feature.schedule.domain.repository

import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.example.garoon_pre.feature.schedule.domain.model.ScheduleInput
import com.example.garoon_pre.core.model.user.GaroonUser
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {

    fun observeSchedulesByDate(
        date: String,
        userIds: List<String> = emptyList()
    ): Flow<List<Schedule>>

    fun observeSchedulesInRange(
        startDate: String,
        endDate: String,
        userIds: List<String> = emptyList()
    ): Flow<List<Schedule>>

    suspend fun syncSchedulesByDate(
        date: String,
        userIds: List<String> = emptyList()
    )

    fun observeScheduleDetail(id: String): Flow<Schedule?>

    suspend fun refreshScheduleDetail(id: String)

    suspend fun createSchedule(input: ScheduleInput)

    suspend fun getUsers(): List<GaroonUser>

    suspend fun updateSchedule(
        scheduleId: String,
        input: ScheduleInput
    )
}