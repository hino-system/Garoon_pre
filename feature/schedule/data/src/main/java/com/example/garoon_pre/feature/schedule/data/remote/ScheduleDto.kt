package com.example.garoon_pre.feature.schedule.data.remote

import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScheduleListResponse(
    val items: List<ScheduleDto>
)

@JsonClass(generateAdapter = true)
data class ScheduleDto(
    val id: String,
    val title: String,
    val startAt: String,
    val endAt: String,
    val repeatRule: String = "なし",
    val location: String? = null,
    val description: String? = null,
    val ownerUserId: String? = null,
    val organizerName: String
)

fun ScheduleDto.toDomain(): Schedule {
    return Schedule(
        id = id,
        title = title,
        startAt = startAt,
        endAt = endAt,
        repeatRule = repeatRule,
        location = location,
        description = description,
        ownerUserId = ownerUserId,
        organizerName = organizerName
    )
}