package com.example.garoon_pre.feature.board.presentation.postlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.garoon_pre.feature.board.domain.model.BoardCategory
import com.example.garoon_pre.feature.board.domain.model.BoardPost
import com.example.garoon_pre.feature.board.domain.repository.BoardRepository
import com.example.garoon_pre.feature.board.presentation.common.boardStatusLabel
import com.example.garoon_pre.feature.board.presentation.common.formatBoardDateTime
import com.example.garoon_pre.feature.board.presentation.common.formatBoardPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BoardPostListUiState(
    val loading: Boolean = true,
    val category: BoardCategory? = null,
    val items: List<BoardPost> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class BoardPostListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BoardRepository
) : ViewModel() {

    private val categoryId: String = checkNotNull(savedStateHandle["categoryId"])

    private val _uiState = MutableStateFlow(BoardPostListUiState())
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
                repository.getBoardPosts(categoryId)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        category = result.category,
                        items = result.items
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = e.message ?: "掲示一覧の取得に失敗しました"
                    )
                }
            }
        }
    }
}

@Composable
fun BoardPostListRoute(
    onBack: () -> Unit,
    onOpenCreate: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    viewModel: BoardPostListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BoardPostListScreen(
        uiState = uiState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onOpenCreate = onOpenCreate,
        onOpenDetail = onOpenDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardPostListScreen(
    uiState: BoardPostListUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenCreate: (String) -> Unit,
    onOpenDetail: (String) -> Unit
) {
    val category = uiState.category

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category?.name ?: "掲示一覧") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("戻る")
                    }
                },
                actions = {
                    TextButton(onClick = onRefresh) {
                        Text("更新")
                    }
                    if (category?.canPost == true) {
                        TextButton(onClick = { onOpenCreate(category.id) }) {
                            Text("＋")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F7FC))
                .padding(padding)
        ) {
            if (uiState.loading && uiState.items.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    category?.let { currentCategory ->
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = currentCategory.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (currentCategory.description.isNotBlank()) {
                                        Text(
                                            text = currentCategory.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF4C5B6F)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    uiState.errorMessage?.let { message ->
                        item {
                            Text(
                                text = message,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (uiState.items.isEmpty() && uiState.errorMessage == null) {
                        item {
                            Text(
                                text = "掲示はありません。",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFF5F6F86)
                            )
                        }
                    }

                    items(uiState.items) { item ->
                        BoardPostCard(
                            item = item,
                            onClick = { onOpenDetail(item.id) }
                        )
                    }
                }
            }

            if (uiState.loading && uiState.items.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
private fun BoardPostCard(
    item: BoardPost,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D2A3A)
                )

                Surface(
                    color = when (item.status) {
                        "active" -> Color(0xFFE8F7EA)
                        "upcoming" -> Color(0xFFFFF4BF)
                        "expired" -> Color(0xFFF0F2F5)
                        else -> Color(0xFFF0F2F5)
                    }
                ) {
                    Text(
                        text = boardStatusLabel(item.status),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF4C5B6F)
                    )
                }
            }

            Text(
                text = "${item.authorName} ・ ${formatBoardDateTime(item.updatedAt)}",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF6C7A8E)
            )

            Text(
                text = formatBoardPeriod(item.startAt, item.endAt),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF5F6F86)
            )

            item.targetDepartment1?.takeIf { it.isNotBlank() }?.let { department ->
                Text(
                    text = "対象部門: $department",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF5F6F86)
                )
            }

            Text(
                text = item.body,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2C3A4D)
            )

            Text(
                text = "コメント ${item.commentCount} 件",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF6C7A8E)
            )
        }
    }
}