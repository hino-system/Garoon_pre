package com.example.garoon_pre.feature.availability.preference.store

import com.example.garoon_pre.feature.availability.domain.model.AvailabilityPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AzurePreferenceStoreAdapter @Inject constructor(
    // 例: Azure Functions client など
) : PreferenceStoreAdapter {

    override suspend fun load(ownerUserId: String): AvailabilityPreference {
        error("Not implemented yet. Intended backend: Azure Functions + Cosmos DB")
    }

    override suspend fun save(ownerUserId: String, preference: AvailabilityPreference) {
        error("Not implemented yet. Intended backend: Azure Functions + Cosmos DB")
    }
}