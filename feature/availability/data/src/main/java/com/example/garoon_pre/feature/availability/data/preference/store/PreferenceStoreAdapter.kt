package com.example.garoon_pre.feature.availability.preference.store

import com.example.garoon_pre.feature.availability.domain.model.AvailabilityPreference

interface PreferenceStoreAdapter {
    suspend fun load(ownerUserId: String): AvailabilityPreference
    suspend fun save(ownerUserId: String, preference: AvailabilityPreference)
}