package com.example.garoon_pre.feature.schedule.domain.model

data class Schedule(
    val id: String,
    val title: String,
    val startAt: String,
    val endAt: String,
    val repeatRule: String = "なし",
    val location: String?,
    val description: String?,
    val ownerUserId: String? = null,
    val organizerName: String
)