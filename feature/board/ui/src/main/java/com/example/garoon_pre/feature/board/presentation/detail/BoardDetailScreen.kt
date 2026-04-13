package com.example.garoon_pre.feature.board.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.garoon_pre.feature.board.domain.model.BoardComment
import com.example.garoon_pre.feature.board.domain.model.BoardPost
import com.example.garoon_pre.feature.board.domain.repository.BoardRepository
import com.example.garoon_pre.feature.board.presentation.common.boardStatusLabel
import com.example.garoon_pre.feature.board.presentation.common.formatBoardDateTime
import com.example.garoon_pre.feature.board.presentation.common.formatBoardPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BoardDetailUiState(
    val loading: Boolean = true,
    val post: BoardPost? = null,
    val commentBody: String = "",
    val errorMessage: String? = null,
    val actionLoading: Boolean = false
)

@HiltViewModel
class BoardDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BoardRepository
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _uiState = MutableStateFlow(BoardDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _message = MutableSharedFlow<String>()
    val message = _message.asSharedFlow()

    private val _deleted = MutableSharedFlow<Unit>()
    val deleted = _deleted.asSharedFlow()

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
                repository.getBoardPostDetail(postId)
            }.onSuccess { post ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        post = post,
                        errorMessage = null
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        post = null,
                        errorMessage = e.message ?: "掲示詳細の取得に失敗しました"
                    )
                }
            }
        }
    }

    fun onCommentBodyChanged(value: String) {
        _uiState.update { it.copy(commentBody = value) }
    }

    fun submitComment() {
        val body = uiState.value.commentBody.trim()
        val post = uiState.value.post ?: return

        if (body.isBlank()) {
            viewModelScope.launch {
                _message.emit("コメントを入力してください")
            }
            return
        }

        if (!post.canComment) {
            viewModelScope.launch {
                _message.emit("コメントできません")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }

            runCatching {
                repository.createBoardComment(post.id, body)
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        commentBody = "",
                        actionLoading = false
                    )
                }
                refresh()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false) }
                _message.emit(e.message ?: "コメントの投稿に失敗しました")
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }

            runCatching {
                repository.deleteBoardComment(commentId)
            }.onSuccess {
                _uiState.update { it.copy(actionLoading = false) }
                refresh()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false) }
                _message.emit(e.message ?: "コメントの削除に失敗しました")
            }
        }
    }

    fun deletePost() {
        val post = uiState.value.post ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }

            runCatching {
                repository.deleteBoardPost(post.id)
            }.onSuccess {
                _uiState.update { it.copy(actionLoading = false) }
                _deleted.emit(Unit)
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false) }
                _message.emit(e.message ?: "掲示の削除に失敗しました")
            }
        }
    }
}

@Composable
fun BoardDetailRoute(
    onBack: () -> Unit,
    onOpenEdit: (String) -> Unit,
    viewModel: BoardDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.message.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.deleted.collect {
            onBack()
        }
    }

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

    BoardDetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onOpenEdit = onOpenEdit,
        onCommentBodyChanged = viewModel::onCommentBodyChanged,
        onSubmitComment = viewModel::submitComment,
        onDeleteComment = viewModel::deleteComment,
        onDeletePost = viewModel::deletePost
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDetailScreen(
    uiState: BoardDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onOpenEdit: (String) -> Unit,
    onCommentBodyChanged: (String) -> Unit,
    onSubmitComment: () -> Unit,
    onDeleteComment: (String) -> Unit,
    onDeletePost: () -> Unit
) {
    val post = uiState.post
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("掲示を削除") },
            text = { Text("この掲示を削除しますか？ コメントも削除されます。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeletePost()
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("掲示詳細") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("戻る")
                    }
                },
                actions = {
                    if (post?.canEdit == true) {
                        TextButton(onClick = { onOpenEdit(post.id) }) {
                            Text("編集")
                        }
                    }
                    if (post?.canDelete == true) {
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Text("削除")
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
            if (uiState.loading && post == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (post == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.errorMessage ?: "掲示が見つかりません")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                    text = post.categoryName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF5F6F86)
                                )
                                Text(
                                    text = post.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = when (post.status) {
                                        "active" -> Color(0xFFE8F7EA)
                                        "upcoming" -> Color(0xFFFFF4BF)
                                        "expired" -> Color(0xFFF0F2F5)
                                        else -> Color(0xFFF0F2F5)
                                    }
                                ) {
                                    Text(
                                        text = boardStatusLabel(post.status),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                Text(
                                    text = "${post.authorName} ・ ${formatBoardDateTime(post.updatedAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6C7A8E)
                                )
                                Text(
                                    text = formatBoardPeriod(post.startAt, post.endAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6C7A8E)
                                )
                                post.targetDepartment1?.takeIf { it.isNotBlank() }?.let { department ->
                                    Text(
                                        text = "対象部門: $department",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6C7A8E)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "本文",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = post.body,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF2C3A4D)
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "コメント",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )

                                if (post.canComment) {
                                    OutlinedTextField(
                                        value = uiState.commentBody,
                                        onValueChange = onCommentBodyChanged,
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("コメントを入力") },
                                        minLines = 3
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = onSubmitComment) {
                                            Text("投稿")
                                        }
                                    }
                                } else {
                                    Text(
                                        text = if (post.allowComments) {
                                            "この掲示にはコメントできません。"
                                        } else {
                                            "この掲示はコメント不可です。"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF6C7A8E)
                                    )
                                }

                                HorizontalDivider()

                                if (post.comments.isEmpty()) {
                                    Text(
                                        text = "コメントはありません。",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF6C7A8E)
                                    )
                                }
                            }
                        }
                    }

                    items(post.comments) { comment ->
                        CommentCard(
                            item = comment,
                            onDelete = { onDeleteComment(comment.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if ((uiState.loading && post != null) || uiState.actionLoading) {
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
private fun CommentCard(
    item: BoardComment,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.authorName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = formatBoardDateTime(item.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6C7A8E)
                )
            }

            Text(
                text = item.body,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2C3A4D)
            )

            if (item.canDelete) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDelete) {
                        Text("削除")
                    }
                }
            }
        }
    }
}