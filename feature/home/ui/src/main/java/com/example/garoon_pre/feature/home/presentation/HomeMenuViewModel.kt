package com.example.garoon_pre.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.data.nextSevenDateStrings
import com.example.core.common.data.toWeekHeaderLabel
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

data class WeekPreviewDay(
    val date: String,
    val label: String,
    val items: List<Schedule>
)

data class HomeMenuUiState(
    val loading: Boolean = true,
    val weekItems: List<WeekPreviewDay> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HomeMenuViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeMenuUiState())
    val uiState = _uiState.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private var observeJob: Job? = null
    private var syncJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
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
                return@launch
            }

            val dates = nextSevenDateStrings()

            _uiState.update {
                it.copy(
                    loading = true,
                    errorMessage = null
                )
            }

            observeJob = launch {
                repository.observeSchedulesInRange(
                    startDate = dates.first(),
                    endDate = dates.last(),
                    userIds = listOf(authUserId)
                ).collect { schedules ->
                    val grouped = schedules.groupBy { it.startAt.take(8) }
                    val week = dates.map { date ->
                        WeekPreviewDay(
                            date = date,
                            label = toWeekHeaderLabel(date),
                            items = grouped[date].orEmpty()
                                .sortedWith(compareBy<Schedule>({ it.startAt }, { it.organizerName }, { it.title }))
                        )
                    }

                    _uiState.update {
                        it.copy(
                            loading = false,
                            weekItems = week,
                            errorMessage = null
                        )
                    }
                }
            }

            syncJob = launch {
                runCatching {
                    dates.forEach { date ->
                        repository.syncSchedulesByDate(
                            date = date,
                            userIds = listOf(authUserId)
                        )
                    }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            errorMessage = e.message ?: "週間予定の取得に失敗しました"
                        )
                    }
                }
            }
        }
    }

    fun onDummyFeatureClicked(title: String) {
        viewModelScope.launch {
            _message.emit("「$title」は準備中です")
        }
    }
}