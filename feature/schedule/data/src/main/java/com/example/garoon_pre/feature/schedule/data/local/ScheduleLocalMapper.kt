package com.example.garoon_pre.feature.schedule.data.local

import com.example.garoon_pre.feature.schedule.data.remote.ScheduleDto
import com.example.garoon_pre.feature.schedule.domain.model.Schedule

fun ScheduleDto.toOccurrenceEntity(requestedDateKey: String? = null): ScheduleOccurrenceEntity {
    val normalizedStartAt = startAt.trim()
    val normalizedDateKey = normalizedStartAt
        .takeIf { it.length >= 8 }
        ?.take(8)
        ?: requestedDateKey.orEmpty()

    return ScheduleOccurrenceEntity(
        occurrenceId = "$id#$normalizedStartAt",
        scheduleId = id,
        dateKey = normalizedDateKey,
        title = title,
        startAt = startAt,
        endAt = endAt,
        repeatRule = repeatRule,
        location = location,
        description = description,
        ownerUserId = ownerUserId,
        organizerName = organizerName,
        syncedAtEpochMillis = System.currentTimeMillis()
    )
}

fun ScheduleDto.toDetailEntity(): ScheduleDetailEntity {
    return ScheduleDetailEntity(
        scheduleId = id,
        title = title,
        startAt = startAt,
        endAt = endAt,
        repeatRule = repeatRule,
        location = location,
        description = description,
        ownerUserId = ownerUserId,
        organizerName = organizerName,
        syncedAtEpochMillis = System.currentTimeMillis()
    )
}

fun ScheduleOccurrenceEntity.toDomain(): Schedule {
    return Schedule(
        id = scheduleId,
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

fun ScheduleDetailEntity.toDomain(): Schedule {
    return Schedule(
        id = scheduleId,
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