package com.example.garoon_pre.feature.availability.preference.store

import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.availability.domain.model.AvailabilityPreference
import com.example.garoon_pre.feature.availability.data.preference.remote.AvailabilityPreferenceApi
import com.example.garoon_pre.feature.availability.data.preference.remote.toDomain
import com.example.garoon_pre.feature.availability.data.preference.remote.toDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ApiPreferenceStoreAdapter @Inject constructor(
    private val api: AvailabilityPreferenceApi,
    private val sessionStore: SessionStore
) : PreferenceStoreAdapter {

    override suspend fun load(ownerUserId: String): AvailabilityPreference {
        val token = sessionStore.tokenFlow.first()
        if (token.isBlank()) return AvailabilityPreference()

        return api.getAvailabilityPreference(
            authorization = "Bearer $token"
        ).toDomain()
    }

    override suspend fun save(ownerUserId: String, preference: AvailabilityPreference) {
        val token = sessionStore.tokenFlow.first()
        if (token.isBlank()) return

        api.putAvailabilityPreference(
            authorization = "Bearer $token",
            request = preference.toDto()
        )
    }
}