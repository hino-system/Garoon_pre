package com.example.garoon_pre.feature.board.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garoon_pre.feature.board.domain.model.BoardCategory
import com.example.garoon_pre.feature.board.domain.repository.BoardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BoardListUiState(
    val loading: Boolean = true,
    val items: List<BoardCategory> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class BoardListViewModel @Inject constructor(
    private val repository: BoardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loading = true,
                    errorMessage = null
                )
            }

            runCatching {
                repository.getBoardCategories()
            }.onSuccess { items ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        items = items
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = e.message ?: "カテゴリーの取得に失敗しました"
                    )
                }
            }
        }
    }
}