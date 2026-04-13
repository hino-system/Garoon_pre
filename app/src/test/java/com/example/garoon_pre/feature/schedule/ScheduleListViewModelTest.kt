package com.example.garoon_pre.feature.schedule

import com.example.garoon_pre.MainDispatcherRule
import com.example.garoon_pre.core.network.SessionStore
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import com.example.garoon_pre.feature.schedule.presentation.list.ScheduleListViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ScheduleListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ScheduleRepository>()
    private val sessionStore = mockk<SessionStore>(relaxed = true)

    @Test
    fun move_week_keeps_same_day_column_selection() = runTest {
        every { repository.todaySchedules } returns MutableStateFlow(emptyList())
        coEvery { repository.getSchedulesByDate(any(), any()) } returns emptyList()

        val viewModel = ScheduleListViewModel(
            repository = repository,
            sessionStore = sessionStore
        )

        advanceUntilIdle()

        val initialState = viewModel.uiState.value
        val selectedIndex = initialState.weekDates.indexOf(initialState.selectedDate)
            .coerceAtLeast(0)

        viewModel.moveWeek(1)
        advanceUntilIdle()

        val nextState = viewModel.uiState.value

        assertEquals(7, nextState.weekDates.size)
        assertEquals(nextState.weekDates[selectedIndex], nextState.selectedDate)
        assertTrue(nextState.itemsByDate.keys.containsAll(nextState.weekDates))
    }
}