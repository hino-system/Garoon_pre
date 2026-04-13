package com.example.garoon_pre.feature.schedule.presentation.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.data.defaultEndAt
import com.example.core.common.data.defaultStartAt
import com.example.core.common.data.isEndAfterStart
import com.example.core.common.data.isValidApiDateTime
import com.example.core.common.data.plusMinutes
import com.example.garoon_pre.feature.schedule.domain.model.ScheduleInput
import com.example.garoon_pre.feature.schedule.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class CreateScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

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

    private val _saved = MutableSharedFlow<Unit>()
    val saved = _saved.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

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
        viewModelScope.launch {
            val validationMessage = validateInput()
            if (validationMessage != null) {
                _errorMessage.emit(validationMessage)
                return@launch
            }

            runCatching {
                repository.createSchedule(
                    ScheduleInput(
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
                    else -> e.message ?: "登録に失敗しました"
                }
                _errorMessage.emit(message)
            }
        }
    }

    private fun validateInput(): String? {
        if (title.isBlank()) return "タイトルを入力してください"

        if (!isValidApiDateTime(startAt.trim())) {
            return "開始日時が不正です"
        }

        if (!isValidApiDateTime(endAt.trim())) {
            return "終了日時が不正です"
        }

        if (!isEndAfterStart(startAt.trim(), endAt.trim())) {
            return "終了日時は開始日時より後にしてください"
        }

        return null
    }
}