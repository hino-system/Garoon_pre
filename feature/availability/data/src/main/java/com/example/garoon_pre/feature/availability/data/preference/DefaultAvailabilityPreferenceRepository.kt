package com.example.garoon_pre.feature.availability.data.preference

import com.example.garoon_pre.feature.availability.domain.model.AvailabilityPreference
import com.example.garoon_pre.feature.availability.preference.store.PreferenceStoreAdapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAvailabilityPreferenceRepository @Inject constructor(
    private val adapter: PreferenceStoreAdapter
) : AvailabilityPreferenceRepository {

    override suspend fun load(ownerUserId: String): AvailabilityPreference {
        return adapter.load(ownerUserId)
    }

    override suspend fun save(ownerUserId: String, preference: AvailabilityPreference) {
        adapter.save(ownerUserId, preference)
    }
}