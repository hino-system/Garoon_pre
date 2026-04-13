package com.example.garoon_pre.feature.schedule.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.common.data.calendarFromApiDateTime
import com.example.core.common.data.formatPickerDateTime
import com.example.garoon_pre.core.designsystem.theme.AppDivider
import com.example.garoon_pre.core.designsystem.theme.BrandBlue
import com.example.garoon_pre.core.designsystem.theme.BrandBlueTint
import com.example.garoon_pre.core.designsystem.theme.TextPrimary
import com.example.garoon_pre.core.designsystem.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun ScheduleDetailRoute(
    onBack: () -> Unit,
    onOpenEdit: (String) -> Unit,
    viewModel: ScheduleDetailViewModel = hiltViewModel()
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

    ScheduleDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onOpenEdit = onOpenEdit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDetailScreen(
    uiState: ScheduleDetailUiState,
    onBack: () -> Unit,
    onOpenEdit: (String) -> Unit
) {
    val item = uiState.item

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "予定詳細",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("戻る")
                    }
                },
                actions = {
                    if (uiState.canEdit && item != null) {
                        TextButton(onClick = { onOpenEdit(item.id) }) {
                            Text("編集")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "予定データが見つかりませんでした。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HeroCard(
                    title = item.title,
                    organizerName = item.organizerName,
                    dateLabel = formatDetailDate(item.startAt),
                    timeRangeLabel = "${formatDetailTime(item.startAt)} - ${formatDetailTime(item.endAt)}",
                    canEdit = uiState.canEdit
                )

                DetailSectionCard {
                    DetailRow(
                        label = "開始",
                        value = formatPickerDateTime(item.startAt)
                    )
                    HorizontalDivider(color = AppDivider)
                    DetailRow(
                        label = "終了",
                        value = formatPickerDateTime(item.endAt)
                    )
                    HorizontalDivider(color = AppDivider)
                    DetailRow(
                        label = "繰り返し",
                        value = item.repeatRule
                    )
                    HorizontalDivider(color = AppDivider)
                    DetailRow(
                        label = "場所",
                        value = item.location?.takeIf { it.isNotBlank() } ?: "未設定"
                    )
                }

                DetailSectionCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "説明",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                        Text(
                            text = item.description?.takeIf { it.isNotBlank() } ?: "説明はありません",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                    }
                }

                if (uiState.canEdit) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = BrandBlueTint
                    ) {
                        TextButton(
                            onClick = { onOpenEdit(item.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("この予定を編集")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    title: String,
    organizerName: String,
    dateLabel: String,
    timeRangeLabel: String,
    canEdit: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailPill(text = "主催: $organizerName")
                if (canEdit) {
                    DetailPill(text = "自分の予定")
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandBlue
                )
                Text(
                    text = timeRangeLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DetailPill(
    text: String
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = BrandBlueTint
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = BrandBlue
        )
    }
}

@Composable
private fun DetailSectionCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
    }
}

private val detailTimeZone = TimeZone.getTimeZone("Asia/Tokyo")

private fun formatDetailDate(value: String): String {
    val calendar = calendarFromApiDateTime(value) ?: return formatPickerDateTime(value)
    return SimpleDateFormat("M月d日(E)", Locale.JAPAN).apply {
        timeZone = detailTimeZone
    }.format(calendar.time)
}

private fun formatDetailTime(value: String): String {
    val calendar = calendarFromApiDateTime(value) ?: return value
    return SimpleDateFormat("HH:mm", Locale.JAPAN).apply {
        timeZone = detailTimeZone
    }.format(calendar.time)
}