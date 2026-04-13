package com.example.garoon_pre.feature.availability.data.preference

import com.example.garoon_pre.feature.availability.domain.model.AvailabilityPreference

interface AvailabilityPreferenceRepository {
    suspend fun load(ownerUserId: String): AvailabilityPreference
    suspend fun save(ownerUserId: String, preference: AvailabilityPreference)
}