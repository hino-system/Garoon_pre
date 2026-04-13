package com.example.garoon_pre.feature.board.presentation.list

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
import com.example.garoon_pre.feature.board.domain.model.BoardCategory

@Composable
fun BoardListRoute(
    onBack: () -> Unit,
    onOpenCategory: (String) -> Unit,
    viewModel: BoardListViewModel = hiltViewModel()
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

    BoardListScreen(
        uiState = uiState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onOpenCategory = onOpenCategory
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardListScreen(
    uiState: BoardListUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenCategory: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("掲示板") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("戻る")
                    }
                },
                actions = {
                    TextButton(onClick = onRefresh) {
                        Text("更新")
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
                    item {
                        Text(
                            text = "閲覧できるカテゴリー",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5F6F86)
                        )
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
                                text = "表示できるカテゴリーがありません。",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Color(0xFF5F6F86)
                            )
                        }
                    }

                    items(uiState.items) { item ->
                        BoardCategoryCard(
                            item = item,
                            onClick = { onOpenCategory(item.id) }
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
private fun BoardCategoryCard(
    item: BoardCategory,
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D2A3A)
                )

                if (item.canPost) {
                    Surface(
                        color = Color(0xFFE8F7EA)
                    ) {
                        Text(
                            text = "投稿可",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF2B7A3D)
                        )
                    }
                }
            }

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4C5B6F)
                )
            }

            Text(
                text = "公開中 ${item.activePostCount} 件 / 全 ${item.totalPostCount} 件",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF6C7A8E)
            )
        }
    }
}