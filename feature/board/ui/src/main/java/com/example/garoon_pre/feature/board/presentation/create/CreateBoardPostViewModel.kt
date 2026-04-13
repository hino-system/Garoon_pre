package com.example.garoon_pre.feature.board.presentation.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garoon_pre.feature.board.domain.model.BoardCategory
import com.example.garoon_pre.feature.board.domain.repository.BoardRepository
import com.example.garoon_pre.feature.board.presentation.common.defaultBoardEndAt
import com.example.garoon_pre.feature.board.presentation.common.defaultBoardStartAt
import com.example.garoon_pre.feature.board.presentation.common.isBoardEndAfterStart
import com.example.garoon_pre.feature.board.presentation.common.isValidBoardDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class CreateBoardPostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BoardRepository
) : ViewModel() {

    private val initialCategoryId: String? = savedStateHandle["categoryId"]

    var categories by mutableStateOf<List<BoardCategory>>(emptyList())
        private set

    var selectedCategoryId by mutableStateOf("")
        private set

    var targetDepartment1 by mutableStateOf("")
        private set

    var title by mutableStateOf("")
        private set

    var body by mutableStateOf("")
        private set

    var startAt by mutableStateOf(defaultBoardStartAt())
        private set

    var endAt by mutableStateOf(defaultBoardEndAt())
        private set

    var allowComments by mutableStateOf(true)
        private set

    var loading by mutableStateOf(false)
        private set

    private val _saved = MutableSharedFlow<Unit>()
    val saved = _saved.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            runCatching {
                repository.getBoardCategories().filter { it.canPost }
            }.onSuccess { items ->
                categories = items
                selectedCategoryId =
                    items.firstOrNull { it.id == initialCategoryId }?.id
                        ?: items.firstOrNull()?.id.orEmpty()
            }.onFailure {
                _errorMessage.emit(it.message ?: "カテゴリーの取得に失敗しました")
            }
        }
    }

    fun onCategoryChanged(value: String) {
        selectedCategoryId = value
        if (value != "cat-department") {
            targetDepartment1 = ""
        }
    }

    fun onTargetDepartment1Changed(value: String) {
        targetDepartment1 = value
    }

    fun onTitleChanged(value: String) {
        title = value
    }

    fun onBodyChanged(value: String) {
        body = value
    }

    fun onStartAtChanged(value: String) {
        startAt = value
    }

    fun onEndAtChanged(value: String) {
        endAt = value
    }

    fun onAllowCommentsChanged(value: Boolean) {
        allowComments = value
    }

    fun save() {
        if (loading) return

        viewModelScope.launch {
            val validationMessage = validate()
            if (validationMessage != null) {
                _errorMessage.emit(validationMessage)
                return@launch
            }

            loading = true

            runCatching {
                repository.createBoardPost(
                    categoryId = selectedCategoryId,
                    title = title.trim(),
                    body = body.trim(),
                    startAt = startAt.trim(),
                    endAt = endAt.trim(),
                    allowComments = allowComments,
                    targetDepartment1 = targetDepartment1.trim().ifBlank { null }
                )
            }.onSuccess {
                _saved.emit(Unit)
            }.onFailure { e ->
                val message = when (e) {
                    is HttpException -> {
                        e.response()?.errorBody()?.string() ?: "HTTP ${e.code()} ${e.message()}"
                    }
                    else -> e.message ?: "掲示の作成に失敗しました"
                }
                _errorMessage.emit(message)
            }

            loading = false
        }
    }

    private fun validate(): String? {
        if (selectedCategoryId.isBlank()) return "カテゴリーを選択してください"
        if (title.isBlank()) return "タイトルを入力してください"
        if (body.isBlank()) return "本文を入力してください"
        if (!isValidBoardDateTime(startAt.trim())) return "掲載開始日時が不正です"
        if (!isValidBoardDateTime(endAt.trim())) return "掲載終了日時が不正です"
        if (!isBoardEndAfterStart(startAt.trim(), endAt.trim())) {
            return "掲載終了日時は掲載開始日時より後にしてください"
        }
        return null
    }
}