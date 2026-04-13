package com.example.garoon_pre.feature.schedule.presentation.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.data.defaultEndAt
import com.example.core.common.data.defaultStartAt
import com.example.core.common.data.isEndAfterStart
import com.example.core.common.data.isValidApiDateTime
import com.example.core.common.data.plusMinutes
import com.example.garoon_pre.core.datastore.SessionStore
import com.example.garoon_pre.feature.schedule.domain.model.Schedule
import com.example.garoon_pre.feature.schedule.domain.model.ScheduleInput
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class EditScheduleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ScheduleRepository,
    private val sessionStore: SessionStore
) : ViewModel() {

    private val scheduleId: String = checkNotNull(savedStateHandle["id"])
    private var source: Schedule? = null

    var title by mutableStateOf("")
        private set
    var startAt by mutableStateOf(defaultStartAt())
        private set
    var endAt by mutableStateOf(defaultEndAt())
        private set
    var repeatRule by mutableStateOf("なし")
        private set
    var location by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set

    private val _saved = MutableSharedFlow<Unit>()
    val saved = _saved.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        loadSource()
    }

    fun onTitleChanged(value: String) {
        title = value
    }

    fun onStartAtChanged(value: String) {
        startAt = value
        if (!isEndAfterStart(startAt, endAt)) {
            endAt = plusMinutes(startAt, 60)
        }
    }

    fun onEndAtChanged(value: String) {
        endAt = value
    }

    fun onRepeatRuleChanged(value: String) {
        repeatRule = value
    }

    fun onLocationChanged(value: String) {
        location = value
    }

    fun onDescriptionChanged(value: String) {
        description = value
    }

    fun save() {
        if (loading) return

        viewModelScope.launch {
            val item = source ?: runCatching {
                val cached = repository.observeScheduleDetail(scheduleId).first()
                cached ?: run {
                    repository.refreshScheduleDetail(scheduleId)
                    repository.observeScheduleDetail(scheduleId)
                        .filterNotNull()
                        .first()
                }
            }.getOrNull()

            if (item == null) {
                _errorMessage.emit("予定データが見つかりませんでした")
                return@launch
            }

            source = item

            val currentAuthUserId = sessionStore.authUserIdFlow.first().trim()
            if (item.ownerUserId != currentAuthUserId) {
                _errorMessage.emit("自分の予定のみ変更できます")
                return@launch
            }

            val validationMessage = validateInput()
            if (validationMessage != null) {
                _errorMessage.emit(validationMessage)
                return@launch
            }

            loading = true

            runCatching {
                repository.updateSchedule(
                    scheduleId = scheduleId,
                    input = ScheduleInput(
                        title = title.trim(),
                        startAt = startAt.trim(),
                        endAt = endAt.trim(),
                        repeatRule = repeatRule,
                        location = location.trim().ifBlank { null },
                        description = description.trim().ifBlank { null }
                    )
                )
            }.onSuccess {
                _saved.emit(Unit)
            }.onFailure { e ->
                val message = when (e) {
                    is HttpException -> {
                        e.response()?.errorBody()?.string()
                            ?: "HTTP ${e.code()} ${e.message()}"
                    }
                    else -> e.message ?: "更新に失敗しました"
                }
                _errorMessage.emit(message)
            }

            loading = false
        }
    }

    private fun loadSource() {
        viewModelScope.launch {
            loading = true

            runCatching {
                val cached = repository.observeScheduleDetail(scheduleId).first()
                cached ?: run {
                    repository.refreshScheduleDetail(scheduleId)
                    repository.observeScheduleDetail(scheduleId)
                        .filterNotNull()
                        .first()
                }
            }.onSuccess { item ->
                source = item
                title = item.title
                startAt = item.startAt
                endAt = item.endAt
                repeatRule = item.repeatRule
                location = item.location.orEmpty()
                description = item.description.orEmpty()
            }.onFailure {
                _errorMessage.emit(it.message ?: "予定データが見つかりませんでした")
            }

            loading = false
        }
    }

    private fun validateInput(): String? {
        if (title.isBlank()) return "タイトルを入力してください"
        if (!isValidApiDateTime(startAt.trim())) return "開始日時が不正です"
        if (!isValidApiDateTime(endAt.trim())) return "終了日時が不正です"
        if (!isEndAfterStart(startAt.trim(), endAt.trim())) {
            return "終了日時は開始日時より後にしてください"
        }
        return null
    }
}