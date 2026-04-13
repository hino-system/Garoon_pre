package com.example.garoon_pre.feature.schedule.data.remote

import com.example.garoon_pre.feature.schedule.domain.model.ScheduleInput

fun ScheduleInput.toRequest(): CreateScheduleRequest {
    return CreateScheduleRequest(
        title = title,
        startAt = startAt,
        endAt = endAt,
        repeatRule = repeatRule,
        location = location,
        description = description
    )
}