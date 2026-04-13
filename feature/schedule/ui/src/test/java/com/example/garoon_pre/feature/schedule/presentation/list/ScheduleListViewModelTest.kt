package com.example.garoon_pre.feature.schedule.presentation.list

import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import com.example.garoon_pre.feature.schedule.presentation.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ScheduleRepository>()
    private val sessionStore = mockk<SessionStore>()

    @Test
    fun refresh_loads_week_items_from_repository() = runTest {
        every { sessionStore.authUserIdFlow } returns flowOf("auth-1")

        val schedule = Schedule(
            id = "s1",
            title = "定例会議",
            startAt = "202604130900",
            endAt = "202604131000",
            repeatRule = "なし",
            location = "会議室A",
            description = "weekly",
            ownerUserId = "auth-1",
            organizerName = "田中"
        )

        every {
            repository.observeSchedulesInRange(
                any(),
                any(),
                any()
            )
        } returns flowOf(listOf(schedule))

        coEvery {
            repository.syncSchedulesByDate(
                any(),
                any()
            )
        } returns Unit

        val viewModel = ScheduleListViewModel(
            repository = repository,
            sessionStore = sessionStore
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.loading)
        assertEquals("定例会議", state.itemsByDate.values.flatten().first().title)

        coVerify(atLeast = 1) {
            repository.syncSchedulesByDate(any(), listOf("auth-1"))
        }
    }

    @Test
    fun refresh_sets_error_when_auth_user_is_missing() = runTest {
        every { sessionStore.authUserIdFlow } returns flowOf("")

        every {
            repository.observeSchedulesInRange(
                any(),
                any(),
                any()
            )
        } returns emptyFlow()

        val viewModel = ScheduleListViewModel(
            repository = repository,
            sessionStore = sessionStore
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.loading)
        assertEquals("ログイン情報がありません", state.errorMessage)
    }
}