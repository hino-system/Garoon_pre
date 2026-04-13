package com.example.garoon_pre.feature.schedule.presentation.create

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.core.common.data.calendarFromApiDateTime
import com.example.core.common.data.currentCalendar
import com.example.core.common.data.formatApiDateTime
import com.example.core.common.data.formatPickerDateTime
import com.example.garoon_pre.feature.schedule.presentation.edit.EditScheduleViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

private enum class ScheduleEditorField {
    Title,
    Location,
    Description
}

private val RepeatOptions = listOf(
    "なし",
    "毎日",
    "営業日（月〜金）",
    "毎週",
    "毎月",
    "毎年"
)

@Composable
fun CreateScheduleRoute(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateScheduleViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.saved.collectLatest {
            onSaved()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    CreateScheduleScreen(
        snackbarHostState = snackbarHostState,
        editorTitle = "新しいイベント",
        title = viewModel.title,
        startAt = viewModel.startAt,
        endAt = viewModel.endAt,
        repeatRule = viewModel.repeatRule,
        location = viewModel.location,
        description = viewModel.description,
        onTitleChanged = viewModel::onTitleChanged,
        onStartAtChanged = viewModel::onStartAtChanged,
        onEndAtChanged = viewModel::onEndAtChanged,
        onRepeatRuleChanged = viewModel::onRepeatRuleChanged,
        onLocationChanged = viewModel::onLocationChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onSave = viewModel::save,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleScreen(
    snackbarHostState: SnackbarHostState,
    editorTitle: String = "新しいイベント",
    title: String,
    startAt: String,
    endAt: String,
    repeatRule: String,
    location: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onStartAtChanged: (String) -> Unit,
    onEndAtChanged: (String) -> Unit,
    onRepeatRuleChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val openDateTimePicker: (String, (String) -> Unit) -> Unit = { initialValue, onSelected ->
        val initialCalendar = calendarFromApiDateTime(initialValue) ?: currentCalendar()

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = (initialCalendar.clone() as Calendar).apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }

                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        selectedCalendar.apply {
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onSelected(formatApiDateTime(selectedCalendar))
                    },
                    initialCalendar.get(Calendar.HOUR_OF_DAY),
                    initialCalendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            initialCalendar.get(Calendar.YEAR),
            initialCalendar.get(Calendar.MONTH),
            initialCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    var editingField by remember { mutableStateOf<ScheduleEditorField?>(null) }
    var showRepeatDialog by remember { mutableStateOf(false) }

    when (editingField) {
        ScheduleEditorField.Title -> {
            TextEditDialog(
                title = "タイトル",
                initialValue = title,
                placeholder = "タイトルを追加",
                singleLine = true,
                onDismiss = { editingField = null },
                onConfirm = {
                    onTitleChanged(it)
                    editingField = null
                }
            )
        }

        ScheduleEditorField.Location -> {
            TextEditDialog(
                title = "場所",
                initialValue = location,
                placeholder = "場所を追加",
                singleLine = true,
                onDismiss = { editingField = null },
                onConfirm = {
                    onLocationChanged(it)
                    editingField = null
                }
            )
        }

        ScheduleEditorField.Description -> {
            TextEditDialog(
                title = "説明",
                initialValue = description,
                placeholder = "説明を追加",
                singleLine = false,
                onDismiss = { editingField = null },
                onConfirm = {
                    onDescriptionChanged(it)
                    editingField = null
                }
            )
        }

        null -> Unit
    }

    if (showRepeatDialog) {
        RepeatRuleDialog(
            selected = repeatRule,
            onDismiss = { showRepeatDialog = false },
            onSelected = {
                onRepeatRuleChanged(it)
                showRepeatDialog = false
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF2F2F7),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = editorTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("キャンセル")
                    }
                },
                actions = {
                    TextButton(onClick = onSave) {
                        Text("完了")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            SectionCard {
                SelectionRow(
                    label = "タイトル",
                    value = title.ifBlank { "タイトルを追加" },
                    isPlaceholder = title.isBlank(),
                    onClick = { editingField = ScheduleEditorField.Title }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SectionCard {
                SelectionRow(
                    label = "開始",
                    value = formatPickerDateTime(startAt),
                    onClick = {
                        openDateTimePicker(startAt, onStartAtChanged)
                    }
                )

                HorizontalDivider(color = Color(0xFFE5E5EA))

                SelectionRow(
                    label = "終了",
                    value = formatPickerDateTime(endAt),
                    onClick = {
                        openDateTimePicker(endAt, onEndAtChanged)
                    }
                )

                HorizontalDivider(color = Color(0xFFE5E5EA))

                SelectionRow(
                    label = "繰り返し",
                    value = repeatRule,
                    onClick = { showRepeatDialog = true }
                )

                HorizontalDivider(color = Color(0xFFE5E5EA))

                SelectionRow(
                    label = "場所",
                    value = location.ifBlank { "場所を追加" },
                    isPlaceholder = location.isBlank(),
                    onClick = { editingField = ScheduleEditorField.Location }
                )

                HorizontalDivider(color = Color(0xFFE5E5EA))

                SelectionRow(
                    label = "説明",
                    value = description.ifBlank { "説明を追加" },
                    isPlaceholder = description.isBlank(),
                    onClick = { editingField = ScheduleEditorField.Description },
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SelectionRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaceholder: Boolean = false,
    maxLines: Int = 1
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF1C1C1E)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isPlaceholder) Color(0xFF8E8E93) else Color(0xFF1C1C1E),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )

        Box(
            modifier = Modifier.padding(start = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFB0B0B5)
            )
        }
    }
}

@Composable
private fun TextEditDialog(
    title: String,
    initialValue: String,
    placeholder: String,
    singleLine: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                singleLine = singleLine,
                minLines = if (singleLine) 1 else 4
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value.trim()) }) {
                Text("完了")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
private fun RepeatRuleDialog(
    selected: String,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("繰り返し") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RepeatOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (option == selected) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

@Composable
fun EditScheduleRoute(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: EditScheduleViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.saved.collectLatest {
            onSaved()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    CreateScheduleScreen(
        snackbarHostState = snackbarHostState,
        editorTitle = "イベントの修正",
        title = viewModel.title,
        startAt = viewModel.startAt,
        endAt = viewModel.endAt,
        repeatRule = viewModel.repeatRule,
        location = viewModel.location,
        description = viewModel.description,
        onTitleChanged = viewModel::onTitleChanged,
        onStartAtChanged = viewModel::onStartAtChanged,
        onEndAtChanged = viewModel::onEndAtChanged,
        onRepeatRuleChanged = viewModel::onRepeatRuleChanged,
        onLocationChanged = viewModel::onLocationChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onSave = viewModel::save,
        onBack = onBack
    )
}