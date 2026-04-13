package com.example.garoon_pre.feature.schedule.data.remote

data class CreateScheduleRequest(
    val title: String,
    val startAt: String,
    val endAt: String,
    val repeatRule: String = "なし",
    val location: String? = null,
    val description: String? = null
)