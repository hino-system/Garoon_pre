package com.example.garoon_pre.feature.schedule.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.data.currentWeekStartDateString
import com.example.core.common.data.shiftWeekDate
import com.example.core.common.data.todayString
import com.example.core.common.data.weekDatesFrom
import com.example.core.common.data.weekStartDateString
import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScheduleListUiState(
    val loading: Boolean = true,
    val weekStartDate: String = currentWeekStartDateString(),
    val weekDates: List<String> = weekDatesFrom(currentWeekStartDateString()),
    val selectedDate: String = todayString(),
    val itemsByDate: Map<String, List<Schedule>> = emptyMap(),
    val errorMessage: String? = null
)

@HiltViewModel
class ScheduleListViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleListUiState())
    val uiState = _uiState.asStateFlow()

    private val _loggedOut = MutableSharedFlow<Unit>()
    val loggedOut = _loggedOut.asSharedFlow()

    private var observeJob: Job? = null
    private var syncJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        loadWeek(
            weekStartDate = _uiState.value.weekStartDate,
            selectedDate = _uiState.value.selectedDate
        )
    }

    fun moveWeek(weekOffset: Int) {
        val current = _uiState.value
        val currentSelectedIndex = current.weekDates.indexOf(current.selectedDate).coerceAtLeast(0)
        val nextWeekStartDate = shiftWeekDate(current.weekStartDate, weekOffset)
        val nextWeekDates = weekDatesFrom(nextWeekStartDate)
        val nextSelectedDate = if (weekOffset == 0) {
            todayString()
        } else {
            nextWeekDates.getOrElse(currentSelectedIndex) { nextWeekDates.first() }
        }

        loadWeek(
            weekStartDate = nextWeekStartDate,
            selectedDate = nextSelectedDate
        )
    }

    fun jumpToToday() {
        val today = todayString()
        loadWeek(
            weekStartDate = weekStartDateString(today),
            selectedDate = today
        )
    }

    fun selectDate(date: String) {
        _uiState.update {
            it.copy(selectedDate = date)
        }
    }

    private fun loadWeek(
        weekStartDate: String,
        selectedDate: String
    ) {
        observeJob?.cancel()
        syncJob?.cancel()

        viewModelScope.launch {
            val authUserId = sessionStore.authUserIdFlow.first().trim()
            if (authUserId.isBlank()) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = "ログイン情報がありません"
                    )
                }
                _loggedOut.emit(Unit)
                return@launch
            }

            val weekDates = weekDatesFrom(weekStartDate)
            val normalizedSelectedDate = selectedDate.takeIf { it in weekDates }
                ?: weekDates.firstOrNull()
                ?: todayString()

            _uiState.update {
                it.copy(
                    loading = true,
                    weekStartDate = weekStartDate,
                    weekDates = weekDates,
                    selectedDate = normalizedSelectedDate,
                    errorMessage = null
                )
            }

            observeJob = launch {
                repository.observeSchedulesInRange(
                    startDate = weekDates.first(),
                    endDate = weekDates.last(),
                    userIds = listOf(authUserId)
                ).collect { schedules ->
                    val itemsByDate = weekDates.toItemsByDate(schedules)
                    _uiState.update {
                        it.copy(
                            loading = false,
                            itemsByDate = itemsByDate,
                            errorMessage = null
                        )
                    }
                }
            }

            syncJob = launch {
                runCatching {
                    weekDates.forEach { date ->
                        repository.syncSchedulesByDate(
                            date = date,
                            userIds = listOf(authUserId)
                        )
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorMessage = throwable.message ?: "予定の取得に失敗しました"
                        )
                    }
                }
            }
        }
    }

    private fun List<String>.toItemsByDate(
        schedules: List<Schedule>
    ): Map<String, List<Schedule>> {
        val grouped = schedules.groupBy { schedule ->
            schedule.startAt.take(8)
        }

        return associateWith { date ->
            grouped[date].orEmpty()
                .sortedWith(compareBy<Schedule>({ it.startAt }, { it.organizerName }, { it.title }))
        }
    }
}