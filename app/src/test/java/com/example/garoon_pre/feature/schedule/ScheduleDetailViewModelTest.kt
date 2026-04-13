package com.example.garoon_pre.feature.schedule

import androidx.lifecycle.SavedStateHandle
import com.example.garoon_pre.MainDispatcherRule
import com.example.garoon_pre.core.network.SessionStore
import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import com.example.garoon_pre.feature.schedule.presentation.detail.ScheduleDetailViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ScheduleDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ScheduleRepository>()
    private val sessionStore = mockk<SessionStore>()

    @Test
    fun owner_schedule_can_edit_true() = runTest {
        val schedule = Schedule(
            id = "sch-1",
            title = "定例MTG",
            startAt = "2026-04-08T10:00:00+09:00",
            endAt = "2026-04-08T11:00:00+09:00",
            location = "会議室A",
            description = null,
            ownerUserId = "emp-001",
            organizerName = "田中"
        )

        every { repository.getCachedById("sch-1") } returns schedule
        every { sessionStore.authUserIdFlow } returns flowOf("emp-001")

        val viewModel = ScheduleDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("id" to "sch-1")),
            repository = repository,
            sessionStore = sessionStore
        )

        advanceUntilIdle()

        assertEquals("sch-1", viewModel.uiState.value.item?.id)
        assertTrue(viewModel.uiState.value.canEdit)
    }
}