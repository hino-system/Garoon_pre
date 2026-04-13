package com.example.garoon_pre.feature.availability.data.preference.remote

import com.example.garoon_pre.feature.availability.domain.model.AvailabilityPreference
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AvailabilityPreferenceDto(
    val selectedUserIds: List<String> = emptyList(),
    val selectedDepartment: String? = null,
    val selectedSection: String? = null,
    val selectedDate: String? = null,
    val weekStartDate: String? = null
)

fun AvailabilityPreferenceDto.toDomain(): AvailabilityPreference {
    return AvailabilityPreference(
        selectedUserIds = selectedUserIds,
        selectedDepartment = selectedDepartment,
        selectedSection = selectedSection,
        selectedDate = selectedDate,
        weekStartDate = weekStartDate
    )
}

fun AvailabilityPreference.toDto(): AvailabilityPreferenceDto {
    return AvailabilityPreferenceDto(
        selectedUserIds = selectedUserIds,
        selectedDepartment = selectedDepartment,
        selectedSection = selectedSection,
        selectedDate = selectedDate,
        weekStartDate = weekStartDate
    )
}