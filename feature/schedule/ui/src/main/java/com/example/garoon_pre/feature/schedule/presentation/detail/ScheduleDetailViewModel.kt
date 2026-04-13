package com.example.garoon_pre.feature.schedule.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

data class ScheduleDetailUiState(
    val loading: Boolean = true,
    val item: Schedule? = null,
    val canEdit: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ScheduleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ScheduleRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val scheduleId: String = checkNotNull(savedStateHandle["id"])
    private val occurrenceStartAt: String? = savedStateHandle["occurrenceStartAt"]
    private val occurrenceEndAt: String? = savedStateHandle["occurrenceEndAt"]

    private val _uiState = MutableStateFlow(ScheduleDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loading = true,
                errorMessage = null
            )

            runCatching {
                val cached = repository.observeScheduleDetail(scheduleId).first()
                val item = cached ?: run {
                    repository.refreshScheduleDetail(scheduleId)
                    repository.observeScheduleDetail(scheduleId)
                        .filterNotNull()
                        .first()
                }

                val currentAuthUserId = sessionStore.authUserIdFlow.first().trim()
                val canEdit = !item.ownerUserId.isNullOrBlank() &&
                        item.ownerUserId == currentAuthUserId

                val displayItem = if (
                    item.repeatRule != "なし" &&
                    !occurrenceStartAt.isNullOrBlank() &&
                    !occurrenceEndAt.isNullOrBlank()
                ) {
                    item.copy(
                        startAt = occurrenceStartAt,
                        endAt = occurrenceEndAt
                    )
                } else {
                    item
                }

                ScheduleDetailUiState(
                    loading = false,
                    item = displayItem,
                    canEdit = canEdit,
                    errorMessage = null
                )
            }.onSuccess { state ->
                _uiState.value = state
            }.onFailure { e ->
                _uiState.value = ScheduleDetailUiState(
                    loading = false,
                    item = null,
                    canEdit = false,
                    errorMessage = e.message ?: "予定データの取得に失敗しました"
                )
            }
        }
    }
}