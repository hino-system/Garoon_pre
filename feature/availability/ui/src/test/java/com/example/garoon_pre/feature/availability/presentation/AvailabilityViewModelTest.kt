package com.example.garoon_pre.feature.availability.presentation

import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.core.model.user.GaroonUser
import com.example.garoon_pre.feature.availability.data.preference.AvailabilityPreferenceRepository
import com.example.garoon_pre.feature.availability.domain.model.AvailabilityPreference
import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AvailabilityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ScheduleRepository>()
    private val sessionStore = mockk<SessionStore>()
    private val preferenceRepository = mockk<AvailabilityPreferenceRepository>()

    @Test
    fun refresh_loads_users_preference_and_schedule_items() = runTest {
        every { sessionStore.userIdFlow } returns flowOf("login-user-id")

        val users = listOf(
            GaroonUser(
                id = "garoon-1",
                userId = "login-user-id",
                displayName = "田中太郎",
                department1 = "営業",
                department2 = "第一営業部"
            ),
            GaroonUser(
                id = "garoon-2",
                userId = "other-user-id",
                displayName = "佐藤花子",
                department1 = "開発",
                department2 = "開発一課"
            )
        )

        coEvery { preferenceRepository.load("login-user-id") } returns AvailabilityPreference(
            selectedUserIds = listOf("garoon-1"),
            selectedDate = "20260413",
            weekStartDate = "20260413"
        )

        coEvery {
            preferenceRepository.save(
                ownerUserId = any(),
                preference = any()
            )
        } returns Unit

        coEvery { repository.getUsers() } returns users

        every {
            repository.observeSchedulesInRange(
                any(),
                any(),
                any()
            )
        } returns flowOf(
            listOf(
                Schedule(
                    id = "s1",
                    title = "定例会議",
                    startAt = "202604130900",
                    endAt = "202604131000",
                    repeatRule = "なし",
                    location = "会議室A",
                    description = "weekly",
                    ownerUserId = "garoon-1",
                    organizerName = "田中太郎"
                )
            )
        )

        coEvery {
            repository.syncSchedulesByDate(
                any(),
                any()
            )
        } returns Unit

        val viewModel = AvailabilityViewModel(
            repository = repository,
            sessionStore = sessionStore,
            preferenceRepository = preferenceRepository
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.loading)
        assertEquals(2, state.users.size)
        assertEquals(listOf("garoon-1"), state.selectedUserIds)
        assertEquals("20260413", state.selectedDate)
        assertEquals("定例会議", state.itemsByDate["20260413"]?.firstOrNull()?.title)

        coVerify(atLeast = 1) { repository.getUsers() }
        coVerify(atLeast = 1) { repository.syncSchedulesByDate(any(), listOf("garoon-1")) }
    }

    @Test
    fun addUser_updates_selected_user_ids() = runTest {
        every { sessionStore.userIdFlow } returns flowOf("login-user-id")

        val users = listOf(
            GaroonUser(
                id = "garoon-1",
                userId = "login-user-id",
                displayName = "田中太郎",
                department1 = "営業",
                department2 = "第一営業部"
            ),
            GaroonUser(
                id = "garoon-2",
                userId = "other-user-id",
                displayName = "佐藤花子",
                department1 = "開発",
                department2 = "開発一課"
            )
        )

        coEvery { preferenceRepository.load("login-user-id") } returns AvailabilityPreference(
            selectedUserIds = listOf("garoon-1"),
            selectedDate = "20260413",
            weekStartDate = "20260413"
        )

        coEvery {
            preferenceRepository.save(
                ownerUserId = any(),
                preference = any()
            )
        } returns Unit

        coEvery { repository.getUsers() } returns users

        every {
            repository.observeSchedulesInRange(
                any(),
                any(),
                any()
            )
        } returns flowOf(emptyList())

        coEvery {
            repository.syncSchedulesByDate(
                any(),
                any()
            )
        } returns Unit

        val viewModel = AvailabilityViewModel(
            repository = repository,
            sessionStore = sessionStore,
            preferenceRepository = preferenceRepository
        )

        advanceUntilIdle()

        viewModel.addUser("garoon-2")
        advanceUntilIdle()

        assertEquals(listOf("garoon-1", "garoon-2"), viewModel.uiState.value.selectedUserIds)
    }

    @Test
    fun removeUser_updates_selected_user_ids() = runTest {
        every { sessionStore.userIdFlow } returns flowOf("login-user-id")

        val users = listOf(
            GaroonUser(
                id = "garoon-1",
                userId = "login-user-id",
                displayName = "田中太郎",
                department1 = "営業",
                department2 = "第一営業部"
            ),
            GaroonUser(
                id = "garoon-2",
                userId = "other-user-id",
                displayName = "佐藤花子",
                department1 = "開発",
                department2 = "開発一課"
            )
        )

        coEvery { preferenceRepository.load("login-user-id") } returns AvailabilityPreference(
            selectedUserIds = listOf("garoon-1", "garoon-2"),
            selectedDate = "20260413",
            weekStartDate = "20260413"
        )

        coEvery {
            preferenceRepository.save(
                ownerUserId = any(),
                preference = any()
            )
        } returns Unit

        coEvery { repository.getUsers() } returns users

        every {
            repository.observeSchedulesInRange(
                any(),
                any(),
                any()
            )
        } returns flowOf(emptyList())

        coEvery {
            repository.syncSchedulesByDate(
                any(),
                any()
            )
        } returns Unit

        val viewModel = AvailabilityViewModel(
            repository = repository,
            sessionStore = sessionStore,
            preferenceRepository = preferenceRepository
        )

        advanceUntilIdle()

        viewModel.removeUser("garoon-2")
        advanceUntilIdle()

        assertEquals(listOf("garoon-1"), viewModel.uiState.value.selectedUserIds)
    }
}