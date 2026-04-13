package com.example.garoon_pre.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.common.data.toTimeText
import kotlinx.coroutines.flow.collectLatest

data class HomeMenuItem(
    val title: String,
    val emoji: String,
    val isEnabled: Boolean = false
)

@Composable
fun HomeMenuRoute(
    onOpenScheduleList: () -> Unit,
    onOpenBoardList: () -> Unit,
    onOpenAvailability: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeMenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.message.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
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

    HomeMenuScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onOpenScheduleList = onOpenScheduleList,
        onOpenBoardList = onOpenBoardList,
        onOpenAvailability = onOpenAvailability,
        onLogout = onLogout,
        onDummyFeatureClick = viewModel::onDummyFeatureClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMenuScreen(
    uiState: HomeMenuUiState,
    snackbarHostState: SnackbarHostState,
    onOpenScheduleList: () -> Unit,
    onOpenBoardList: () -> Unit,
    onOpenAvailability: () -> Unit,
    onLogout: () -> Unit,
    onDummyFeatureClick: (String) -> Unit
) {
    val menuItems = listOf(
        HomeMenuItem("スケジュール", "📅", isEnabled = true),
        HomeMenuItem("メール", "✉️"),
        HomeMenuItem("メッセージ", "💬"),
        HomeMenuItem("スペース", "🫧"),
        HomeMenuItem("掲示板", "📋", isEnabled = true),
        HomeMenuItem("空き時間の確認", "🗓️", isEnabled = true),
        HomeMenuItem("ワークフロー", "🧾"),
        HomeMenuItem("PC表示", "🖥️"),
        HomeMenuItem("編集", "⚙️")
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text("Pre_Garoon") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("ログアウト")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEAF4FF))
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("メニュー")
                }

                item {
                    MenuGrid(
                        items = menuItems,
                        onScheduleClick = onOpenScheduleList,
                        onBoardClick = onOpenBoardList,
                        onAvailabilityClick = onOpenAvailability,
                        onDummyFeatureClick = onDummyFeatureClick
                    )
                }

                item {
                    Text("週間予定")
                }

                uiState.errorMessage?.let { message ->
                    item {
                        Text(message)
                    }
                }

                items(uiState.weekItems.size) { index ->
                    val day = uiState.weekItems[index]
                    WeekPreviewCard(day = day)
                }
            }

            if (uiState.loading) {
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
private fun MenuGrid(
    items: List<HomeMenuItem>,
    onScheduleClick: () -> Unit,
    onBoardClick: () -> Unit,
    onAvailabilityClick: () -> Unit,
    onDummyFeatureClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable {
                                when (item.title) {
                                    "スケジュール" -> onScheduleClick()
                                    "掲示板" -> onBoardClick()
                                    "空き時間の確認" -> onAvailabilityClick()
                                    else -> onDummyFeatureClick(item.title)
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.emoji,
                                fontSize = 22.sp
                            )

                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (rowItems.size < 3) {
                    repeat(3 - rowItems.size) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekPreviewCard(day: WeekPreviewDay) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(day.label)

            if (day.items.isEmpty()) {
                Text("予定はありません")
            } else {
                day.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.width(64.dp)
                        ) {
                            Text(toTimeText(item.startAt))
                            Text(toTimeText(item.endAt))
                        }

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(width = 4.dp, height = 44.dp)
                                .background(
                                    color = Color(0xFF5AA9FF),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(item.title)
                            item.location?.takeIf { it.isNotBlank() }?.let {
                                Text(it)
                            }
                        }
                    }
                }
            }
        }
    }
}