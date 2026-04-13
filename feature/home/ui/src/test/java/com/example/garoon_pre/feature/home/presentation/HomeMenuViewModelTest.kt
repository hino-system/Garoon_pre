package com.example.garoon_pre.feature.home.presentation

import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeMenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = mockk<ScheduleRepository>()
    private val sessionStore = mockk<SessionStore>()

    @Test
    fun refresh_loads_week_items_from_repository() = runTest {
        every { sessionStore.authUserIdFlow } returns flowOf("auth-1")

        val schedules = listOf(
            Schedule(
                id = "s1",
                title = "朝会",
                startAt = "202604130900",
                endAt = "202604131000",
                repeatRule = "なし",
                location = "会議室A",
                description = "daily meeting",
                ownerUserId = "auth-1",
                organizerName = "田中"
            ),
            Schedule(
                id = "s2",
                title = "夕会",
                startAt = "202604131700",
                endAt = "202604131730",
                repeatRule = "なし",
                location = "会議室B",
                description = "closing",
                ownerUserId = "auth-1",
                organizerName = "佐藤"
            )
        )

        every {
            repository.observeSchedulesInRange(
                any(),
                any(),
                any()
            )
        } returns flowOf(schedules)

        coEvery {
            repository.syncSchedulesByDate(
                any(),
                any()
            )
        } returns Unit

        val viewModel = HomeMenuViewModel(
            repository = repository,
            sessionStore = sessionStore
        )

        advanceUntilIdle()

        val state: HomeMenuUiState = viewModel.uiState.value

        assertFalse(state.loading)
        assertEquals(7, state.weekItems.size)

        val firstDayItems = state.weekItems.firstOrNull { it.date == "20260413" }?.items.orEmpty()
        assertEquals(2, firstDayItems.size)
        assertEquals("朝会", firstDayItems[0].title)
        assertEquals("夕会", firstDayItems[1].title)

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

        val viewModel = HomeMenuViewModel(
            repository = repository,
            sessionStore = sessionStore
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.loading)
        assertEquals("ログイン情報がありません", state.errorMessage)
    }

    @Test
    fun onDummyFeatureClicked_emits_preparing_message() = runTest {
        every { sessionStore.authUserIdFlow } returns flowOf("auth-1")

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

        val viewModel = HomeMenuViewModel(
            repository = repository,
            sessionStore = sessionStore
        )

        advanceUntilIdle()

        val deferredMessage = async {
            viewModel.message.first()
        }

        viewModel.onDummyFeatureClicked("掲示板")
        advanceUntilIdle()

        assertEquals("「掲示板」は準備中です", deferredMessage.await())
    }
}