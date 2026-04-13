package com.example.garoon_pre.feature.availability.domain.model

data class AvailabilityPreference(
    val selectedUserIds: List<String> = emptyList(),
    val selectedDate: String? = null,
    val weekStartDate: String? = null,
    val selectedDepartment: String? = null,
    val selectedSection: String? = null
)