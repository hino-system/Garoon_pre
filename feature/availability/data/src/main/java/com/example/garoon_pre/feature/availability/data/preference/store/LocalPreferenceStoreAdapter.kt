package com.example.garoon_pre.feature.availability.preference.store

import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.availability.domain.model.AvailabilityPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPreferenceStoreAdapter @Inject constructor(
    private val sessionStore: SessionStore
) : PreferenceStoreAdapter {

    override suspend fun load(ownerUserId: String): AvailabilityPreference {
        val selectedUserIds = sessionStore.loadAvailabilitySelection(ownerUserId)

        return AvailabilityPreference(
            selectedUserIds = selectedUserIds
        )
    }

    override suspend fun save(ownerUserId: String, preference: AvailabilityPreference) {
        sessionStore.saveAvailabilitySelection(
            ownerUserId = ownerUserId,
            selectedUserIds = preference.selectedUserIds
        )
    }
}