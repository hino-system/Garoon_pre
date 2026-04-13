package com.example.garoon_pre.feature.schedule.domain.model

data class ScheduleInput(
    val title: String,
    val startAt: String,
    val endAt: String,
    val repeatRule: String = "なし",
    val location: String? = null,
    val description: String? = null
)