package com.example.garoon_pre.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = scope
        ) {
            context.preferencesDataStoreFile("session.preferences_pb")
        }

    private val tokenKey = stringPreferencesKey("api_token")
    private val userIdKey = stringPreferencesKey("user_id")
    private val displayNameKey = stringPreferencesKey("display_name")
    private val department1Key = stringPreferencesKey("department_1")
    private val department2Key = stringPreferencesKey("department_2")
    private val positionKey = stringPreferencesKey("position")
    private val roleKey = stringPreferencesKey("role")
    private val availabilitySelectionOwnerUserIdKey =
        stringPreferencesKey("availability_selection_owner_user_id")
    private val availabilitySelectedUserIdsKey =
        stringPreferencesKey("availability_selected_user_ids")
    private val availabilitySelectedDateKey =
        stringPreferencesKey("availability_selected_date")
    private val availabilityWeekStartDateKey =
        stringPreferencesKey("availability_week_start_date")
    private val availabilitySelectedDepartmentKey =
        stringPreferencesKey("availability_selected_department")
    private val availabilitySelectedSectionKey =
        stringPreferencesKey("availability_selected_section")

    private val authUserIdKey = stringPreferencesKey("auth_user_id")

    val authUserIdFlow: Flow<String> = dataStore.data
        .map { prefs -> prefs[authUserIdKey].orEmpty() }
        .distinctUntilChanged()

    val tokenFlow: Flow<String> = dataStore.data
        .map { prefs -> prefs[tokenKey].orEmpty() }
        .distinctUntilChanged()

    val userIdFlow: Flow<String> = dataStore.data
        .map { prefs -> prefs[userIdKey].orEmpty() }
        .distinctUntilChanged()

    val displayNameFlow: Flow<String> = dataStore.data
        .map { prefs -> prefs[displayNameKey].orEmpty() }
        .distinctUntilChanged()

    val department1Flow: Flow<String> = dataStore.data
        .map { prefs -> prefs[department1Key].orEmpty() }
        .distinctUntilChanged()

    val department2Flow: Flow<String> = dataStore.data
        .map { prefs -> prefs[department2Key].orEmpty() }
        .distinctUntilChanged()

    val positionFlow: Flow<String> = dataStore.data
        .map { prefs -> prefs[positionKey].orEmpty() }
        .distinctUntilChanged()

    val roleFlow: Flow<String> = dataStore.data
        .map { prefs -> prefs[roleKey].orEmpty() }
        .distinctUntilChanged()

    suspend fun saveSession(
        authUserId: String,
        userId: String,
        token: String,
        displayName: String,
        department1: String?,
        department2: String?,
        position: String,
        role: String
    ) {
        dataStore.edit { prefs ->
            prefs[authUserIdKey] = authUserId
            prefs[userIdKey] = userId
            prefs[tokenKey] = token
            prefs[displayNameKey] = displayName
            prefs[department1Key] = department1.orEmpty()
            prefs[department2Key] = department2.orEmpty()
            prefs[positionKey] = position
            prefs[roleKey] = role
        }
    }

    suspend fun saveAvailabilitySelection(
        ownerUserId: String,
        selectedUserIds: List<String>
    ) {
        dataStore.edit { prefs ->
            prefs[availabilitySelectionOwnerUserIdKey] = ownerUserId
            prefs[availabilitySelectedUserIdsKey] = selectedUserIds
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(",")
        }
    }

    suspend fun loadAvailabilitySelection(ownerUserId: String): List<String> {
        val prefs = dataStore.data.first()
        val savedOwnerUserId = prefs[availabilitySelectionOwnerUserIdKey].orEmpty()
        if (savedOwnerUserId != ownerUserId) return emptyList()

        return prefs[availabilitySelectedUserIdsKey]
            .orEmpty()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(tokenKey)
            prefs.remove(userIdKey)
            prefs.remove(displayNameKey)
            prefs.remove(department1Key)
            prefs.remove(department2Key)
            prefs.remove(positionKey)
            prefs.remove(roleKey)
            prefs.remove(authUserIdKey)

            // availabilitySelectionOwnerUserIdKey と
            // availabilitySelectedUserIdsKey は消さない
            // 同じユーザーで再ログインした時に前回表示を復元するため
        }
    }

    suspend fun saveAvailabilityPreference(
        ownerUserId: String,
        selectedUserIds: List<String>,
        selectedDate: String?,
        weekStartDate: String?,
        selectedDepartment: String?,
        selectedSection: String?
    ) {
        dataStore.edit { prefs ->
            prefs[availabilitySelectionOwnerUserIdKey] = ownerUserId
            prefs[availabilitySelectedUserIdsKey] = selectedUserIds
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(",")

            prefs[availabilitySelectedDateKey] = selectedDate.orEmpty()
            prefs[availabilityWeekStartDateKey] = weekStartDate.orEmpty()
            prefs[availabilitySelectedDepartmentKey] = selectedDepartment.orEmpty()
            prefs[availabilitySelectedSectionKey] = selectedSection.orEmpty()
        }
    }



    suspend fun loadAvailabilitySelectedDate(ownerUserId: String): String? {
        val prefs = dataStore.data.first()
        val savedOwnerUserId = prefs[availabilitySelectionOwnerUserIdKey].orEmpty()
        if (savedOwnerUserId != ownerUserId) return null

        return prefs[availabilitySelectedDateKey]?.takeIf { it.isNotBlank() }
    }

    suspend fun loadAvailabilityWeekStartDate(ownerUserId: String): String? {
        val prefs = dataStore.data.first()
        val savedOwnerUserId = prefs[availabilitySelectionOwnerUserIdKey].orEmpty()
        if (savedOwnerUserId != ownerUserId) return null

        return prefs[availabilityWeekStartDateKey]?.takeIf { it.isNotBlank() }
    }

    suspend fun loadAvailabilitySelectedDepartment(ownerUserId: String): String? {
        val prefs = dataStore.data.first()
        val savedOwnerUserId = prefs[availabilitySelectionOwnerUserIdKey].orEmpty()
        if (savedOwnerUserId != ownerUserId) return null

        return prefs[availabilitySelectedDepartmentKey]?.takeIf { it.isNotBlank() }
    }

    suspend fun loadAvailabilitySelectedSection(ownerUserId: String): String? {
        val prefs = dataStore.data.first()
        val savedOwnerUserId = prefs[availabilitySelectionOwnerUserIdKey].orEmpty()
        if (savedOwnerUserId != ownerUserId) return null

        return prefs[availabilitySelectedSectionKey]?.takeIf { it.isNotBlank() }
    }
}