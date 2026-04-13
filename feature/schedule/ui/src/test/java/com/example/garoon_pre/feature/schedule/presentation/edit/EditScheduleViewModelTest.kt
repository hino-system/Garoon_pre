package com.example.garoon_pre.feature.schedule.presentation.edit

import androidx.lifecycle.SavedStateHandle
import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.example.garoon_pre.feature.schedule.domain.model.ScheduleInput
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import com.example.garoon_pre.feature.schedule.presentation.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditScheduleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<ScheduleRepository>()
    private val sessionStore = mockk<SessionStore>()

    @Test
    fun loadSource_populates_fields_from_repository() = runTest {
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

        every { repository.observeScheduleDetail("s1") } returns flowOf(schedule)
        coEvery { repository.refreshScheduleDetail(any()) } returns Unit
        every { sessionStore.authUserIdFlow } returns flowOf("auth-1")

        val viewModel = EditScheduleViewModel(
            savedStateHandle = SavedStateHandle(mapOf("id" to "s1")),
            repository = repository,
            sessionStore = sessionStore
        )

        advanceUntilIdle()

        assertEquals("定例会議", viewModel.title)
        assertEquals("202604130900", viewModel.startAt)
        assertEquals("202604131000", viewModel.endAt)
        assertEquals("なし", viewModel.repeatRule)
        assertEquals("会議室A", viewModel.location)
        assertEquals("weekly", viewModel.description)
        assertFalse(viewModel.loading)
    }

    @Test
    fun save_emits_error_when_current_user_is_not_owner() = runTest {
        val schedule = Schedule(
            id = "s1",
            title = "定例会議",
            startAt = "202604130900",
            endAt = "202604131000",
            repeatRule = "なし",
            location = "会議室A",
            description = "weekly",
            ownerUserId = "owner-1",
            organizerName = "田中"
        )

        every { repository.observeScheduleDetail("s1") } returns flowOf(schedule)
        coEvery { repository.refreshScheduleDetail(any()) } returns Unit
        every { sessionStore.authUserIdFlow } returns flowOf("auth-2")

        val viewModel = EditScheduleViewModel(
            savedStateHandle = SavedStateHandle(mapOf("id" to "s1")),
            repository = repository,
            sessionStore = sessionStore
        )

        advanceUntilIdle()

        val deferredMessage = async {
            viewModel.errorMessage.first()
        }

        viewModel.save()
        advanceUntilIdle()

        assertEquals("自分の予定のみ変更できます", deferredMessage.await())

        coVerify(exactly = 0) {
            repository.updateSchedule(any(), any())
        }
    }

    @Test
    fun save_updates_schedule_and_emits_saved() = runTest {
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

        every { repository.observeScheduleDetail("s1") } returns flowOf(schedule)
        coEvery { repository.refreshScheduleDetail(any()) } returns Unit
        every { sessionStore.authUserIdFlow } returns flowOf("auth-1")

        coEvery {
            repository.updateSchedule(
                scheduleId = "s1",
                input = match<ScheduleInput> {
                    it.title == "新しい会議" &&
                            it.startAt == "202604130900" &&
                            it.endAt == "202604131000" &&
                            it.repeatRule == "なし" &&
                            it.location == "新会議室" &&
                            it.description == "updated memo"
                }
            )
        } returns Unit

        val viewModel = EditScheduleViewModel(
            savedStateHandle = SavedStateHandle(mapOf("id" to "s1")),
            repository = repository,
            sessionStore = sessionStore
        )

        advanceUntilIdle()

        viewModel.onTitleChanged("新しい会議")
        viewModel.onLocationChanged("新会議室")
        viewModel.onDescriptionChanged("updated memo")

        val deferredSaved = async {
            viewModel.saved.first()
        }

        viewModel.save()
        advanceUntilIdle()

        deferredSaved.await()

        coVerify(exactly = 1) {
            repository.updateSchedule(
                scheduleId = "s1",
                input = any()
            )
        }
    }
}